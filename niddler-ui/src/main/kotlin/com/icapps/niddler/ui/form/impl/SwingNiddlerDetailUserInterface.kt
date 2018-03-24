package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.TabComponent
import com.icapps.niddler.ui.form.detail.MessageDetailPanel
import com.icapps.niddler.ui.form.detail.body.*
import com.icapps.niddler.ui.form.ui.NiddlerDetailUserInterface
import com.icapps.niddler.ui.model.MessageContainer
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.util.BodyFormatType
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingConstants

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
open class SwingNiddlerDetailUserInterface(componentsFactory: ComponentsFactory,
                                           messageContainer: MessageContainer) : NiddlerDetailUserInterface {

    override var message: ParsedNiddlerMessage? = null
        set(value) {
            if (field?.messageId == value?.messageId)
                return
            field = value
            if (value == null)
                clear()
            else
                updateWindowContents(value)
        }

    override val asComponent: Component
        get() = content.asComponent

    private val bodyRoot: JPanel = JPanel(BorderLayout())
    private val detailPanel: MessageDetailPanel = MessageDetailPanel(messageContainer)

    private val content: TabComponent = componentsFactory.createTabComponent()

    override fun init() {
        content.addTab("Details", JScrollPane(detailPanel))
        content.addTab("Body", bodyRoot)

        showNoSelection()
    }

    protected open fun updateWindowContents(currentMessage: ParsedNiddlerMessage) {
        bodyRoot.removeAll()

        detailPanel.setMessage(currentMessage)

        if (currentMessage.body.isNullOrBlank()) {
            showEmptyMessageBody(currentMessage)
        } else {
            when (currentMessage.bodyFormat.type) {
                BodyFormatType.FORMAT_JSON -> bodyRoot.add(NiddlerJsonDataPanel(currentMessage), BorderLayout.CENTER)
                BodyFormatType.FORMAT_XML -> bodyRoot.add(NiddlerXMLDataPanel(currentMessage), BorderLayout.CENTER)
                BodyFormatType.FORMAT_PLAIN -> bodyRoot.add(NiddlerPlainDataPanel(currentMessage), BorderLayout.CENTER)
                BodyFormatType.FORMAT_HTML -> bodyRoot.add(NiddlerHTMLDataPanel(currentMessage), BorderLayout.CENTER)
                BodyFormatType.FORMAT_FORM_ENCODED -> bodyRoot.add(NiddlerFormEncodedPanel(currentMessage), BorderLayout.CENTER)
                BodyFormatType.FORMAT_IMAGE -> bodyRoot.add(NiddlerImageDataPanel(currentMessage), BorderLayout.CENTER)
                BodyFormatType.FORMAT_BINARY -> bodyRoot.add(NiddlerBinaryPanel(currentMessage), BorderLayout.CENTER)
                BodyFormatType.FORMAT_EMPTY -> showEmptyMessageBody(currentMessage)
            }
        }
        bodyRoot.revalidate()
        content.invalidate()
        content.repaint()
    }

    protected open fun showEmptyMessageBody(message: ParsedNiddlerMessage) {
        bodyRoot.removeAll()
        bodyRoot.add(JLabel("This ${if (message.isRequest) "request" else "response"} has no body", SwingConstants.CENTER), BorderLayout.CENTER)
        bodyRoot.revalidate()
        content.invalidate()
        content.repaint()
    }

    protected open fun showNoSelection() {
        bodyRoot.removeAll()
        bodyRoot.add(JLabel("Select a request/response", SwingConstants.CENTER), BorderLayout.CENTER)
        bodyRoot.revalidate()
        content.invalidate()
        content.repaint()
    }

    protected open fun clear() {
        showNoSelection()
        detailPanel.clear()
    }
}