package com.icapps.niddler.ui.form.detail

import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.StackTraceComponent
import com.icapps.niddler.ui.util.ClipboardUtil
import com.jgoodies.forms.layout.CellConstraints
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.layout.RowSpec
import java.awt.BorderLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder


/**
 * @author Nicola Verbeeck
 * @date 18/11/16.
 */
class MessageDetailPanel(private val messages: NiddlerMessageStorage<ParsedNiddlerMessage>, componentsFactory: ComponentsFactory) : JPanel(BorderLayout()) {

    private val generalPanel = JPanel(BorderLayout())
    private val headersPanel = JPanel(BorderLayout())
    private val tracePanel = JPanel(BorderLayout())
    private val generalContentPanel: JPanel
    private val headersContentPanel: JPanel
    private val containerPanel = JPanel()
    private val stackTraceComponent: StackTraceComponent?

    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val boldFont: Font
    private val normalFont: Font
    private val italicFont: Font

    init {
        border = EmptyBorder(5, 5, 5, 5)
        containerPanel.layout = BoxLayout(containerPanel, BoxLayout.Y_AXIS)

        generalPanel.border = BorderFactory.createTitledBorder(EmptyBorder(0, 0, 0, 0),
                "General", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        headersPanel.border = BorderFactory.createTitledBorder(EmptyBorder(0, 0, 0, 0),
                "Headers", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        tracePanel.border = BorderFactory.createTitledBorder(EmptyBorder(0, 0, 0, 0),
                "Stacktrace", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)

        generalContentPanel = JPanel(FormLayout("left:default, 3dlu, pref", "pref, pref, pref, pref, pref"))
        generalContentPanel.border = EmptyBorder(5, 5, 5, 5)
        generalPanel.add(JScrollPane(generalContentPanel))

        headersContentPanel = JPanel(FormLayout("left:default, 3dlu, pref", ""))
        headersContentPanel.border = EmptyBorder(5, 5, 5, 5)
        headersPanel.add(JScrollPane(headersContentPanel))

        containerPanel.add(generalPanel)
        containerPanel.add(headersPanel)
        containerPanel.add(tracePanel)

        stackTraceComponent = componentsFactory.createTraceComponent()?.also {
            tracePanel.add(it.asComponent)
        }

        boldFont = Font("Monospaced", Font.BOLD, 12)
        normalFont = Font("Monospaced", 0, 12)
        italicFont = Font("Monospaced", Font.ITALIC, 12)
    }

    fun setMessage(message: ParsedNiddlerMessage) {
        val other = if (message.isRequest) findResponse(message) else findRequest(message)

        removeAll()

        add(containerPanel, BorderLayout.NORTH)

        val constraints = CellConstraints()

        generalContentPanel.removeAll()
        headersContentPanel.removeAll()

        generalContentPanel.add(boldLabel("Timestamp"), constraints.xy(1, 1))
        generalContentPanel.add(selectableLabel(formatter.format(Date(message.timestamp))), constraints.xy(3, 1))

        generalContentPanel.add(boldLabel("Method"), constraints.xy(1, 2))
        generalContentPanel.add(selectableLabel(message.method ?: other?.method), constraints.xy(3, 2))

        generalContentPanel.add(boldLabel("URL"), constraints.xy(1, 3))
        generalContentPanel.add(selectableLabel(message.url ?: other?.url), constraints.xy(3, 3))

        generalContentPanel.add(boldLabel("Status"), constraints.xy(1, 4))
        generalContentPanel.add(selectableLabel((message.statusCode
                ?: other?.statusCode)?.toString()), constraints.xy(3, 4))

        generalContentPanel.add(boldLabel("Execution time"), constraints.xy(1, 5))
        generalContentPanel.add(makeExecutionTimeLabel(message, other), constraints.xy(3, 5))

        populateHeaders(message)
        populateStackTrace(message)

        headersContentPanel.revalidate()
        generalContentPanel.revalidate()

        revalidate()
        repaint()
    }

    private fun populateHeaders(message: ParsedNiddlerMessage) {
        val layout = headersContentPanel.layout as FormLayout
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
            headersContentPanel.add(selectableBoldLabel(it.key), constraints.xy(1, row))
            headersContentPanel.add(selectableLabel(it.value.joinToString(", ")), constraints.xy(3, row))

            ++row
        }
    }

    private fun populateStackTrace(message: ParsedNiddlerMessage) {
        val request = if (message.isRequest) {
            message
        } else {
            messages.findRequest(message)
        }
        val trace = request?.trace
        if (trace != null)
            stackTraceComponent?.setStackTrace(trace)

        tracePanel.isVisible = trace != null
    }

    fun clear() {
        removeAll()
        add(JLabel("Select a request/response", SwingConstants.CENTER), BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun makeExecutionTimeLabel(firstMessage: ParsedNiddlerMessage?, secondMessage: ParsedNiddlerMessage?): JComponent {
        if (firstMessage == null || secondMessage == null) {
            return italicLabel("Unknown")
        }
        val time = if (firstMessage.timestamp > secondMessage.timestamp)
            firstMessage.timestamp - secondMessage.timestamp
        else
            secondMessage.timestamp - firstMessage.timestamp
        return selectableLabel("$time msec")
    }

    private fun findResponse(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return messages.getMessagesWithRequestId(message.requestId).find {
            !it.isRequest
        }
    }

    private fun findRequest(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return messages.getMessagesWithRequestId(message.requestId).find(ParsedNiddlerMessage::isRequest)
    }

    private fun boldLabel(text: String?): JComponent {
        val label = JLabel(text)
        label.font = boldFont
        return label
    }

    private fun italicLabel(text: String?): JComponent {
        val label = JLabel(text)
        label.font = italicFont
        return label
    }

    private fun selectableLabel(text: String?): JComponent {
        val f = JLabel(text)
        f.font = normalFont
        f.componentPopupMenu = JPopupMenu().apply {

            add(JMenuItem("Copy").apply {
                addActionListener {
                    ClipboardUtil.copyToClipboard(StringSelection(text))
                }
            })
        }
        return f
    }

    private fun selectableBoldLabel(text: String?): JComponent {
        val f = JLabel(text)
        f.font = boldFont
        f.componentPopupMenu = JPopupMenu().apply {

            add(JMenuItem("Copy").apply {
                addActionListener {
                    ClipboardUtil.copyToClipboard(StringSelection(text))
                }
            })
        }
        return f

    }
}