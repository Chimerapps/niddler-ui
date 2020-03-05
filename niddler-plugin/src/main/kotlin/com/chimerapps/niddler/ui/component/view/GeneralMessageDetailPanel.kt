package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ext.headerCase
import com.chimerapps.niddler.ui.util.ui.ClipboardUtil
import com.chimerapps.niddler.ui.util.ui.Popup
import com.chimerapps.niddler.ui.util.ui.PopupAction
import com.chimerapps.niddler.ui.util.ui.action
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
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
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class GeneralMessageDetailPanel(project: Project) : JPanel(BorderLayout()) {

    private companion object {
        private fun createOuterContainer(title: String, inner: JComponent): JPanel {
            val border = BorderFactory.createTitledBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                            EmptyBorder(5, 5, 5, 5)
                    ), title, TitledBorder.ABOVE_TOP, TitledBorder.LEFT)

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
    var currentMessage: ParsedNiddlerMessage? = null
        private set

    init {
        add(contentScroller, BorderLayout.CENTER)
    }

    fun init(message: ParsedNiddlerMessage, other: ParsedNiddlerMessage?) {
        fillGeneral(message, other)
        fillHeaders(message)

        val trace = message.trace
        tracePanel.setStackTrace(trace)

        contextPanel.parent.isVisible = false

        needsResponse = (message.isRequest && other == null)
        currentMessage = message

        revalidate()
        repaint()
    }

    private fun fillGeneral(message: ParsedNiddlerMessage, other: ParsedNiddlerMessage?) {
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
        generalPanel.add(buildValue(message.url ?: other?.url, key = "URL"), constraints.xy(3, 3))

        var row = 4

        (message.url ?: other?.url)?.let { url ->
            try {
                val urlDecoded = URLDecoder.decode(url, "utf-8")
                if (urlDecoded != url) {
                    generalPanel.add(buildLabel("Decoded URL", value = urlDecoded, withPopupMenu = true), constraints.xy(1, row))
                    generalPanel.add(buildValue(urlDecoded, key = "Decoded URL"), constraints.xy(3, row))
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
    }

    private fun fillHeaders(message: ParsedNiddlerMessage) {
        headersPanel.removeAll()

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
        val otherHeaders = if (message.isRequest) {
            message.parsedNetworkRequest?.headers
        } else {
            message.parsedNetworkReply?.headers
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

    private fun buildValue(text: String?, key: String? = null): JBLabel {
        return JBLabel(text ?: "").also {
            it.font = valueFont

            it.componentPopupMenu = makePopup(key ?: "", text ?: "", text ?: "")
        }
    }

    private fun makeExecutionTimeLabel(firstMessage: ParsedNiddlerMessage?, secondMessage: ParsedNiddlerMessage?, key: String): JBLabel {
        if (firstMessage == null || secondMessage == null) {
            return JBLabel("Unknown").also { it.font = italicFont }
        }
        val time = if (firstMessage.timestamp > secondMessage.timestamp)
            firstMessage.timestamp - secondMessage.timestamp
        else
            secondMessage.timestamp - firstMessage.timestamp
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