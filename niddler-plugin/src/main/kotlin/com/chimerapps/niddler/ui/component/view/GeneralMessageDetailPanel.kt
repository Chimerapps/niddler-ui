package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ui.ClipboardUtil
import com.chimerapps.niddler.ui.util.ui.Popup
import com.chimerapps.niddler.ui.util.ui.PopupAction
import com.chimerapps.niddler.ui.util.ui.action
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.containers.isNullOrEmpty
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

    init {
        add(contentScroller, BorderLayout.CENTER)
    }

    fun init(message: ParsedNiddlerMessage, other: ParsedNiddlerMessage?) {
        fillGeneral(message, other)
        fillHeaders(message)

        val trace = message.trace
        if (!trace.isNullOrEmpty()) {
            tracePanel.setStackTrace(trace)
            tracePanel.parent.isVisible = true
        } else {
            tracePanel.parent.isVisible = false
        }
        contextPanel.parent.isVisible = false

        revalidate()
        repaint()
    }

    private fun fillGeneral(message: ParsedNiddlerMessage, other: ParsedNiddlerMessage?) {
        generalPanel.removeAll() //TODO do not aggressively clear all children

        val constraints = CellConstraints()

        generalPanel.add(buildLabel("Timestamp"), constraints.xy(1, 1))
        generalPanel.add(buildValue(timestampFormatter.format(Date(message.timestamp))), constraints.xy(3, 1))

        generalPanel.add(buildLabel("Method"), constraints.xy(1, 2))
        generalPanel.add(buildValue(message.method ?: other?.method), constraints.xy(3, 2))

        generalPanel.add(buildLabel("URL"), constraints.xy(1, 3))
        generalPanel.add(buildValue(message.url ?: other?.url), constraints.xy(3, 3))

        var row = 4

        (message.url ?: other?.url)?.let { url ->
            try {
                val urlDecoded = URLDecoder.decode(url, "utf-8")
                if (urlDecoded != url) {
                    generalPanel.add(buildLabel("Decoded URL"), constraints.xy(1, row))
                    generalPanel.add(buildValue(urlDecoded), constraints.xy(3, row))
                    ++row
                }
            } catch (e: Throwable) {
            }
        }

        generalPanel.add(buildLabel("Status"), constraints.xy(1, row))
        generalPanel.add(buildValue((message.statusCode
                ?: other?.statusCode)?.toString()), constraints.xy(3, row))
        ++row

        generalPanel.add(buildLabel("Execution time"), constraints.xy(1, row))
        generalPanel.add(makeExecutionTimeLabel(message, other), constraints.xy(3, row))
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
        message.headers?.forEach {
            val values = it.value.joinToString(", ")
            headersPanel.add(buildLabel(it.key, value = values, withPopupMenu = true), constraints.xy(1, row))
            headersPanel.add(buildValue(values, key = it.key), constraints.xy(3, row))

            ++row
        }
    }

    private fun buildLabel(text: String, value: String? = null, withPopupMenu: Boolean = false): JBLabel {
        return JBLabel(text).also {
            it.font = labelFont
            if (withPopupMenu) {
                val actions = mutableListOf<PopupAction>()
                actions += "Copy" action { ClipboardUtil.copyToClipboard(StringSelection(text)) }
                if (value != null)
                    actions += "Copy key and value" action { ClipboardUtil.copyToClipboard(StringSelection("$text: $value")) }
                it.componentPopupMenu = Popup(actions)
            }
        }
    }

    private fun buildValue(text: String?, key: String? = null): JBLabel {
        return JBLabel(text ?: "").also {
            it.font = valueFont

            val actions = mutableListOf<PopupAction>()
            actions += "Copy" action { ClipboardUtil.copyToClipboard(StringSelection(text)) }
            if (key != null)
                actions += "Copy key and value" action { ClipboardUtil.copyToClipboard(StringSelection("$key: $text")) }
            it.componentPopupMenu = Popup(actions)
        }
    }

    private fun makeExecutionTimeLabel(firstMessage: ParsedNiddlerMessage?, secondMessage: ParsedNiddlerMessage?): JComponent {
        if (firstMessage == null || secondMessage == null) {
            return JBLabel("Unknown").also { it.font = italicFont }
        }
        val time = if (firstMessage.timestamp > secondMessage.timestamp)
            firstMessage.timestamp - secondMessage.timestamp
        else
            secondMessage.timestamp - firstMessage.timestamp
        return buildValue("$time msec")
    }
}