package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ext.headerCase
import com.chimerapps.niddler.ui.util.ui.ClipboardUtil
import com.chimerapps.niddler.ui.util.ui.Popup
import com.chimerapps.niddler.ui.util.ui.PopupAction
import com.chimerapps.niddler.ui.util.ui.action
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.intellij.ide.IdeTooltip
import com.intellij.ide.IdeTooltipManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBFont
import com.jgoodies.forms.layout.CellConstraints
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.layout.RowSpec
import java.awt.BorderLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TreeMap
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class GeneralMessageDetailPanel(project: Project, private val niddlerMessageContainer: NiddlerMessageContainer) : JPanel(BorderLayout()) {

    private companion object {
        private fun createOuterContainer(title: String, inner: JComponent): JPanel {
            val border = BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                    EmptyBorder(5, 5, 5, 5)
                ), title, TitledBorder.ABOVE_TOP, TitledBorder.LEFT
            )

            return JPanel(BorderLayout()).also {
                it.border = border
                it.add(inner, BorderLayout.CENTER)
            }
        }
    }

    private val generalPanel = JPanel(FormLayout("left:default, 3dlu, pref", "pref, pref, pref, pref, pref, pref"))
    private val headersPanel = JPanel(FormLayout("left:default, 3dlu, pref", "pref, pref, pref, pref, pref, pref"))
    private val tracePanel = StackTraceView(project)
    private val contextPanel = JPanel()
    private val detailContainer = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)

        it.add(createOuterContainer("General", generalPanel))
        it.add(createOuterContainer("Headers", headersPanel))
        it.add(createOuterContainer("Stacktrace", tracePanel))
        it.add(createOuterContainer("Context", contextPanel))
    }

    private val contentScroller = JBScrollPane(JPanel(BorderLayout()).also {
        it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        it.add(detailContainer, BorderLayout.NORTH)
    })

    private val valueFont = JBFont.create(Font("Monospaced", 0, 12))
    private val labelFont = valueFont.asBold()
    private val italicFont = valueFont.asItalic()
    private val timestampFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    var needsResponse: Boolean = false
        private set
    var currentMessage: NiddlerMessageInfo? = null
        private set

    init {
        add(contentScroller, BorderLayout.CENTER)
    }

    fun init(message: NiddlerMessageInfo, other: NiddlerMessageInfo?) {
        val loaded = niddlerMessageContainer.load(message)

        fillGeneral(message, other)
        fillHeaders(loaded, other, message)

        val trace = loaded?.trace
        tracePanel.setStackTrace(trace)

        contextPanel.parent.isVisible = false

        needsResponse = (message.isRequest && other == null)
        currentMessage = message

        revalidate()
        repaint()
    }

    private fun fillGeneral(message: NiddlerMessageInfo, other: NiddlerMessageInfo?) {
        generalPanel.removeAll() //TODO do not aggressively clear all children

        val constraints = CellConstraints()

        val timestampValue = timestampFormatter.format(Date(message.timestamp))
        generalPanel.add(buildLabel("Timestamp", value = timestampValue, withPopupMenu = true), constraints.xy(1, 1))
        generalPanel.add(buildValue(timestampValue, key = "Timestamp"), constraints.xy(3, 1))

        val methodValue = message.method ?: other?.method
        generalPanel.add(buildLabel("Method", value = methodValue, withPopupMenu = true), constraints.xy(1, 2))
        generalPanel.add(buildValue(methodValue, key = "Method"), constraints.xy(3, 2))

        val urlValue = message.url ?: other?.url
        generalPanel.add(buildLabel("URL", value = urlValue, withPopupMenu = true), constraints.xy(1, 3))
        generalPanel.add(buildValue(message.url ?: other?.url, key = "URL", toolTip = makeUrlTooltip(message.url ?: other?.url)), constraints.xy(3, 3))

        var row = 4

        (message.url ?: other?.url)?.let { url ->
            try {
                val urlDecoded = URLDecoder.decode(url, "utf-8")
                if (urlDecoded != url) {
                    generalPanel.add(buildLabel("Decoded URL", value = urlDecoded, withPopupMenu = true), constraints.xy(1, row))
                    generalPanel.add(buildValue(urlDecoded, key = "Decoded URL", toolTip = makeUrlTooltip(url)), constraints.xy(3, row))
                    ++row
                }
            } catch (e: Throwable) {
            }
        }

        val statusMessage = (message.statusCode ?: other?.statusCode)?.toString()
        generalPanel.add(buildLabel("Status", value = statusMessage, withPopupMenu = true), constraints.xy(1, row))
        generalPanel.add(buildValue(statusMessage, key = "Status"), constraints.xy(3, row))
        ++row

        val execTimeValue = makeExecutionTimeLabel(message, other, "Execution time")
        generalPanel.add(buildLabel("Execution time", value = execTimeValue.text, withPopupMenu = true), constraints.xy(1, row))
        generalPanel.add(execTimeValue, constraints.xy(3, row))
        ++row

        makeWaitTimeLabel(message, other, "Wait time")?.let { waitTimeValue ->
            generalPanel.add(buildLabel("Wait time", value = waitTimeValue.text, withPopupMenu = true), constraints.xy(1, row))
            generalPanel.add(waitTimeValue, constraints.xy(3, row))
            ++row
        }
    }

    private fun makeUrlTooltip(url: String?): String? {
        url ?: return null
        try {
            val queryParts = URL(url).query.split("&")
            val urlParameters = queryParts.map { pair ->
                val index = pair.indexOf('=')
                val key = if (index > 0) URLDecoder.decode(pair.substring(0, index), "UTF-8") else pair
                val value = if (index > 0 && pair.length >= index + 1) URLDecoder.decode(pair.substring(index + 1), "UTF-8") else ""
                key to value
            }.groupByTo(TreeMap()) { it.first }.mapValues { it.value.map { value -> value.second } }

            if (urlParameters.isEmpty()) return null

            return buildString {
                append("<html>")
                urlParameters.forEach { (key, values) ->
                    if (values.isEmpty()) {
                        append(key).append("<br/>")
                    } else {
                        values.forEach { value ->
                            append(key).append(" = ").append(value).append("<br/>")
                        }
                    }
                }
                append("</html>")
            }.trim()
        } catch (e: Throwable) {
            return null
        }
    }

    private fun fillHeaders(message: NiddlerMessage?, other: NiddlerMessageInfo?, source: NiddlerMessageInfo) {
        headersPanel.removeAll()
        message ?: return

        val layout = headersPanel.layout as FormLayout
        val numRows = layout.rowCount
        for (i in 0 until numRows) {
            layout.removeRow(1)
        }
        val spec = RowSpec.decodeSpecs("pref")[0]
        for (i in 1..(message.headers?.size ?: 0)) {
            layout.appendRow(spec)
        }
        val constraints = CellConstraints()
        var row = 1
        val messageHeaders = message.headers
        messageHeaders?.forEach {
            if (it.key.toLowerCase() == "x-niddler-debug") return@forEach

            val values = it.value.joinToString(", ")
            val casedKey = it.key.headerCase()
            headersPanel.add(buildLabel(casedKey, value = values, withPopupMenu = true), constraints.xy(1, row))
            headersPanel.add(buildValue(values, key = casedKey), constraints.xy(3, row))

            ++row
        }
        val otherHeaders = if (source.isRequest) {
            other?.networkRequest?.let(niddlerMessageContainer::loadHeaders)
        } else {
            source.networkReply?.let(niddlerMessageContainer::loadHeaders)
        }

        if (!otherHeaders.isNullOrEmpty() && messageHeaders != null) {
            val extra = otherHeaders.filterNot { messageHeaders.containsKey(it.key) && it.value == messageHeaders[it.key] }
            extra.forEach { _ ->
                layout.appendRow(spec)
            }
            extra.forEach {
                val values = it.value.joinToString(", ")
                val casedKey = it.key.headerCase()
                headersPanel.add(buildLabel("* $casedKey", value = values, withPopupMenu = true, rawText = casedKey), constraints.xy(1, row))
                headersPanel.add(buildValue(values, key = casedKey), constraints.xy(3, row))

                ++row
            }
        }
    }

    private fun buildLabel(text: String, value: String? = null, withPopupMenu: Boolean = false, rawText: String = text): JBLabel {
        return JBLabel(text).also {
            it.font = labelFont
            if (withPopupMenu) {
                it.componentPopupMenu = makePopup(rawText, rawText, value)
            }
        }
    }

    private fun buildValue(text: String?, key: String? = null, toolTip: String? = null): JBLabel {
        return JBLabel(text ?: "").also {
            it.font = valueFont

            it.componentPopupMenu = makePopup(key ?: "", text ?: "", text ?: "")

            if (toolTip != null) {
                it.addMouseListener(object : MouseAdapter() {

                    private var toolTipInstance: IdeTooltip? = null

                    override fun mouseEntered(e: MouseEvent) {
                        val tip = object : IdeTooltip(it, e.point, JBLabel(toolTip)) {
                            override fun canBeDismissedOnTimeout(): Boolean {
                                return false
                            }
                        }
                        IdeTooltipManager.getInstance().hide(toolTipInstance)
                        toolTipInstance = IdeTooltipManager.getInstance().show(tip, true)
                    }

                    override fun mouseExited(e: MouseEvent?) {
                        super.mouseExited(e)
                        IdeTooltipManager.getInstance().hide(toolTipInstance)
                        toolTipInstance = null
                    }
                })
            }
        }
    }

    private fun makeExecutionTimeLabel(firstMessage: NiddlerMessageInfo?, secondMessage: NiddlerMessageInfo?, key: String): JBLabel {
        if (firstMessage == null || secondMessage == null) {
            return JBLabel("Unknown").also { it.font = italicFont }
        }
        val time = if (firstMessage.timestamp > secondMessage.timestamp)
            firstMessage.timestamp - secondMessage.timestamp
        else
            secondMessage.timestamp - firstMessage.timestamp
        return buildValue("$time msec", key = key)
    }

    private fun makeWaitTimeLabel(firstMessage: NiddlerMessageInfo?, secondMessage: NiddlerMessageInfo?, key: String): JBLabel? {
        if (firstMessage == null || secondMessage == null) {
            return null
        }
        val time = firstMessage.waitTime ?: secondMessage.waitTime ?: return null
        if (time < 0) return null
        return buildValue("$time msec", key = key)
    }

    private fun makePopup(key: String, valueToCopySolo: String, value: String?): Popup {
        val actions = mutableListOf<PopupAction>()
        actions += "Copy" action { ClipboardUtil.copyToClipboard(StringSelection(valueToCopySolo)) }
        if (value != null)
            actions += "Copy key and value" action { ClipboardUtil.copyToClipboard(StringSelection("$key: $value")) }
        return Popup(actions)
    }
}