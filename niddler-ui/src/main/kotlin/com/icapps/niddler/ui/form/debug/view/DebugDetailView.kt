package com.icapps.niddler.ui.form.debug.view

import com.icapps.niddler.ui.bold
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.debug.content.HeaderEditorPanel
import com.icapps.niddler.ui.form.debug.view.body.BodyEditor
import com.icapps.niddler.ui.left
import com.icapps.niddler.ui.setFixedWidth
import com.icapps.niddler.ui.singleLine
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.text.NumberFormat
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JFormattedTextField
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.text.NumberFormatter


/**
 * @author nicolaverbeeck
 */
class DebugDetailView(componentsFactory: ComponentsFactory) : JPanel() {

    private companion object {
        private val DROP_OVERLAY_COLOR = Color(0.75f, 0.42f, 0.0f, 0.3f)
    }

    internal var inDrop = false

    private val titleView = JLabel("<>")
    private var currentMessage: DebugMessageEntry? = null
    private val headerPanel: HeaderEditorPanel
    private val bodyPanel = BodyEditor(componentsFactory)
    private val responseValuesPanel = JPanel()
    private val codeField = setupCodeField()
    private val messageField = JTextField()

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val handler = DebugDetailDropHandler(this)
        transferHandler = handler
        dropTarget.addDropTargetListener(handler)

        headerPanel = HeaderEditorPanel(componentsFactory) { }
        headerPanel.preferredSize = Dimension(headerPanel.preferredSize.width, 200)
        headerPanel.maximumSize = Dimension(headerPanel.maximumSize.width, 200)
        headerPanel.minimumSize = Dimension(headerPanel.minimumSize.width, 200)

        add(titleView.bold().left())
        add(Box.createVerticalStrut(5))

        responseValuesPanel.layout = BorderLayout()
        responseValuesPanel.add(codeField.setFixedWidth(50).singleLine(), BorderLayout.WEST)
        responseValuesPanel.add(messageField.singleLine(), BorderLayout.CENTER)
        responseValuesPanel.maximumSize = Dimension(responseValuesPanel.maximumSize.width, codeField.maximumSize.height)

        add(JLabel("Headers").bold().left())
        add(Box.createVerticalStrut(2))
        add(headerPanel.left())
        add(Box.createVerticalStrut(5))

        add(JLabel("Body").bold().left())
        add(Box.createVerticalStrut(2))
        add(bodyPanel.left())

        bodyPanel.preferredSize = Dimension(headerPanel.preferredSize.width, 300)
        bodyPanel.maximumSize = Dimension(headerPanel.maximumSize.width, 300)
        bodyPanel.minimumSize = Dimension(headerPanel.minimumSize.width, 300)
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (inDrop) {
            g.color = DROP_OVERLAY_COLOR
            g.fillRect(0, 0, width, height)
        }
    }

    fun clearMessage() {
        currentMessage = null
        bodyPanel.clear()
        repaint()
    }

    fun showDetails(debugMessageEntry: DebugMessageEntry) {
        if (currentMessage === debugMessageEntry)
            return

        currentMessage?.let { save(it) }
        bodyPanel.initWith(debugMessageEntry)

        if (debugMessageEntry.isRequest) {
            removeResponsePanel()
            titleView.text = "Intercepted request"
        } else {
            showResponsePanel()
            codeField.text = debugMessageEntry.response?.statusCode?.toString() ?: ""
            messageField.text = debugMessageEntry.response?.statusLine ?: ""
            titleView.text = "Intercepted response"
        }

        initHeaders(debugMessageEntry)
    }

    fun save(into: DebugMessageEntry) {
        into.modifiedHeaders = headerPanel.extractHeaders()
        into.modifiedBody = bodyPanel.saveBody()
    }

    private fun initHeaders(debugMessageEntry: DebugMessageEntry) {
        val headers = debugMessageEntry.modifiedHeaders ?: debugMessageEntry.response?.headers ?: emptyMap()
        headerPanel.init(headers)
    }

    private fun setupCodeField(): JFormattedTextField {
        val format = NumberFormat.getInstance()
        val formatter = NumberFormatter(format)
        formatter.valueClass = Int::class.javaObjectType
        formatter.minimum = 100
        formatter.maximum = 999
        formatter.allowsInvalid = true
        formatter.commitsOnValidEdit = true
        return JFormattedTextField(formatter)
    }

    private fun removeResponsePanel() {
        if (responseValuesPanel.parent == null)
            return
        remove(2)
        remove(responseValuesPanel)
        invalidate()
        repaint()
    }

    private fun showResponsePanel() {
        if (responseValuesPanel.parent != null)
            return
        add(Box.createVerticalStrut(5), null, 2)
        add(responseValuesPanel, null, 3)
        invalidate()
        repaint()
    }
}