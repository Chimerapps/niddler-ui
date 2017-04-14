package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.form.components.TabComponent
import com.icapps.niddler.ui.model.BodyFormatType
import com.icapps.niddler.ui.model.MessageContainer
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * @author Nicola Verbeeck
 * @date 18/11/16.
 */
class MessageDetailContainer(interfaceFactory: InterfaceFactory, message: MessageContainer) {

    private val bodyRoot: JPanel
    private val detailPanel: MessageDetailPanel
    private var currentMessage: ParsedNiddlerMessage? = null
    private val content: TabComponent

    val asComponent: Component
        get() = content.asComponent

    init {
        content = interfaceFactory.createTabComponent()
        bodyRoot = JPanel(BorderLayout())

        detailPanel = MessageDetailPanel(message)
        content.addTab("Details", detailPanel)
        content.addTab("Body", bodyRoot)
    }

    fun setMessage(message: ParsedNiddlerMessage) {
        if (currentMessage?.messageId == message.messageId)
            return

        currentMessage = message
        bodyRoot.removeAll()
        detailPanel.setMessage(message)

        if (message.bodyFormat.type == BodyFormatType.FORMAT_JSON) {
            bodyRoot.add(NiddlerJsonDataPanel(message), BorderLayout.CENTER)
        } else if (message.bodyFormat.type == BodyFormatType.FORMAT_XML) {
            bodyRoot.add(NiddlerXMLDataPanel(message), BorderLayout.CENTER)
        } else if (message.bodyFormat.type == BodyFormatType.FORMAT_PLAIN) {
            bodyRoot.add(NiddlerPlainDataPanel(message), BorderLayout.CENTER)
        } else if (message.bodyFormat.type == BodyFormatType.FORMAT_FORM_ENCODED) {
            bodyRoot.add(NiddlerFormEncodedPanel(message), BorderLayout.CENTER)
        } else if (message.body.isNullOrBlank()) {
            showEmptyMessageBody()
            return
        }
        bodyRoot.revalidate()
        content.invalidate()
        content.repaint()
    }

    private fun showEmptyMessageBody() {
        bodyRoot.removeAll()
        bodyRoot.add(JLabel("This ${if (currentMessage?.isRequest == true) "request" else "response"} has no body", SwingConstants.CENTER), BorderLayout.CENTER)
        bodyRoot.revalidate()
        content.invalidate()
        content.repaint()
    }

    fun clear() {
        currentMessage = null

        clearBodyPane()
        detailPanel.clear()
    }

    private fun clearBodyPane() {
        bodyRoot.removeAll()
        bodyRoot.add(JLabel("Select a request/response", SwingConstants.CENTER), BorderLayout.CENTER)
        bodyRoot.revalidate()
        content.invalidate()
        content.repaint()
    }

}