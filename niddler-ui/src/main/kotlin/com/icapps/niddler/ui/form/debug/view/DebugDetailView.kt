package com.icapps.niddler.ui.form.debug.view

import com.icapps.niddler.ui.bold
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.debug.content.HeaderEditorPanel
import com.icapps.niddler.ui.left
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

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

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val handler = DebugDetailDropHandler(this)
        transferHandler = handler
        dropTarget.addDropTargetListener(handler)

        headerPanel = HeaderEditorPanel { }
        headerPanel.preferredSize = Dimension(headerPanel.preferredSize.width, 200)
        headerPanel.maximumSize = Dimension(headerPanel.maximumSize.width, 200)
        headerPanel.minimumSize = Dimension(headerPanel.minimumSize.width, 200)

        add(titleView.bold().left())
        add(Box.createVerticalStrut(5))
        add(JLabel("Headers").bold().left())
        add(Box.createVerticalStrut(2))
        add(headerPanel.left())
        add(Box.createVerticalStrut(5))

        add(JLabel("Body").bold().left())
        add(Box.createVerticalStrut(2))
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
    }

    fun showDetails(debugMessageEntry: DebugMessageEntry) {
        if (currentMessage === debugMessageEntry)
            return

        currentMessage?.let { save(it) }

        if (debugMessageEntry.isRequest)
            titleView.text = "Intercepted request"
        else
            titleView.text = "Intercepted response"

        initHeaders(debugMessageEntry)
    }

    private fun save(into: DebugMessageEntry) {

    }

    private fun initHeaders(debugMessageEntry: DebugMessageEntry) {
        val headers = debugMessageEntry.response?.headers ?: return
        headerPanel.init(headers)
    }
}