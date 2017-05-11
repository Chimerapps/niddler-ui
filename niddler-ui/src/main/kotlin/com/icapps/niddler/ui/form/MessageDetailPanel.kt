package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.MessageContainer
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import org.jdesktop.swingx.JXTaskPane
import org.jdesktop.swingx.JXTaskPaneContainer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.border.EmptyBorder


/**
 * @author Nicola Verbeeck
 * @date 18/11/16.
 */
class MessageDetailPanel(private val messages: MessageContainer) : JPanel(BorderLayout()) {

    private val taskContainer: JXTaskPaneContainer = JXTaskPaneContainer()
    private val generalPanel: JXTaskPane = JXTaskPane()
    private val headersPanel: JXTaskPane = JXTaskPane()
    private val generalContentPanel: JPanel
    private val headersContentPanel: JPanel

    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val boldFont: Font
    private val normalFont: Font
    private val italicFont: Font

    init {
        add(taskContainer, BorderLayout.CENTER)
        generalPanel.title = "General"
        headersPanel.title = "Headers"
        taskContainer.add(generalPanel)
        taskContainer.add(headersPanel)

        generalContentPanel = JPanel(BorderLayout())
        generalContentPanel.border = EmptyBorder(5, 5, 5, 5)
        val contentScroller = JScrollPane(generalContentPanel)
        generalPanel.contentPane.add(contentScroller)

        headersContentPanel = JPanel(BorderLayout())
        headersContentPanel.border = EmptyBorder(5, 5, 5, 5)
        val contentScroller2 = JScrollPane(headersContentPanel)
        headersPanel.contentPane.add(contentScroller2)

        boldFont = Font("Monospaced", Font.BOLD, 12)
        normalFont = Font("Monospaced", 0, 12)
        italicFont = Font("Monospaced", Font.ITALIC, 12)
    }

    fun setMessage(message: ParsedNiddlerMessage) {
        val other = if (message.isRequest) findResponse(message) else findRequest(message)

        removeAll()
        generalContentPanel.removeAll()
        headersContentPanel.removeAll()
        add(taskContainer, BorderLayout.CENTER)

        val labelPanel = JPanel()
        val valuePanel = JPanel()
        labelPanel.layout = BoxLayout(labelPanel, BoxLayout.Y_AXIS)
        labelPanel.border = EmptyBorder(0, 0, 0, 10)
        valuePanel.layout = BoxLayout(valuePanel, BoxLayout.Y_AXIS)
        labelPanel.background = Color(0, 0, 0, 0)
        valuePanel.background = Color(0, 0, 0, 0)
        labelPanel.isOpaque = false
        valuePanel.isOpaque = false

        generalContentPanel.add(labelPanel, BorderLayout.WEST)
        generalContentPanel.add(valuePanel, BorderLayout.CENTER)

        labelPanel.add(boldLabel("Timestamp"))
        labelPanel.add(Box.createRigidArea(Dimension(0, 5)))
        valuePanel.add(selectableLabel(formatter.format(Date(message.timestamp))))
        valuePanel.add(Box.createRigidArea(Dimension(0, 5)))

        labelPanel.add(boldLabel("Method"))
        labelPanel.add(Box.createRigidArea(Dimension(0, 5)))
        valuePanel.add(selectableLabel(message.method ?: other?.method))
        valuePanel.add(Box.createRigidArea(Dimension(0, 5)))

        labelPanel.add(boldLabel("URL"))
        labelPanel.add(Box.createRigidArea(Dimension(0, 5)))
        valuePanel.add(selectableLabel(message.url ?: other?.url))
        valuePanel.add(Box.createRigidArea(Dimension(0, 5)))

        labelPanel.add(boldLabel("Status"))
        valuePanel.add(selectableLabel((message.statusCode ?: other?.statusCode)?.toString()))

        labelPanel.add(boldLabel("Execution time"))
        valuePanel.add(makeExecutionTimeLabel(message, other))

        populateHeaders(message)

        headersContentPanel.revalidate()
        generalContentPanel.revalidate()

        revalidate()
        repaint()
    }

    private fun populateHeaders(message: ParsedNiddlerMessage) {
        val labelPanel = JPanel()
        val valuePanel = JPanel()
        labelPanel.layout = BoxLayout(labelPanel, BoxLayout.Y_AXIS)
        labelPanel.border = EmptyBorder(0, 0, 0, 10)
        valuePanel.layout = BoxLayout(valuePanel, BoxLayout.Y_AXIS)
        labelPanel.background = Color(0, 0, 0, 0)
        valuePanel.background = Color(0, 0, 0, 0)
        labelPanel.isOpaque = false
        valuePanel.isOpaque = false
        headersContentPanel.add(labelPanel, BorderLayout.WEST)
        headersContentPanel.add(valuePanel, BorderLayout.CENTER)
        message.headers?.forEach {
            labelPanel.add(selectableBoldLabel(it.key))
            labelPanel.add(Box.createRigidArea(Dimension(0, 5)))

            valuePanel.add(selectableLabel(it.value.joinToString(", ")))
            valuePanel.add(Box.createRigidArea(Dimension(0, 5)))
        }
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
        return messages.getMessagesWithRequestId(message.requestId)?.find {
            !it.isRequest
        }
    }

    private fun findRequest(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return messages.getMessagesWithRequestId(message.requestId)?.find(ParsedNiddlerMessage::isRequest)
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
        val f = JTextField(text)
        f.isEditable = false
        f.border = null
        f.background = Color(0, 0, 0, 0)
        f.foreground = UIManager.getColor("Label.foreground")
        f.font = normalFont
        return f
    }

    private fun selectableBoldLabel(text: String?): JComponent {
        val f = JTextField(text)
        f.isEditable = false
        f.border = null
        f.background = Color(0, 0, 0, 0)
        f.foreground = UIManager.getColor("Label.foreground")
        f.font = boldFont
        return f

    }
}