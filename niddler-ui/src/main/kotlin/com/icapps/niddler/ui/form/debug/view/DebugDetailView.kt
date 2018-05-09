package com.icapps.niddler.ui.form.debug.view

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.debug.content.HeaderEditorPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JPanel

/**
 * @author nicolaverbeeck
 */
class DebugDetailView(componentsFactory: ComponentsFactory) : JPanel(BorderLayout()) {

    private companion object {
        private val DROP_OVERLAY_COLOR = Color(0.75f, 0.42f, 0.0f, 0.3f)
    }

    internal var inDrop = false

    init {
        val handler = DebugDetailDropHandler(this)
        transferHandler = handler
        dropTarget.addDropTargetListener(handler)

        val headerPanel = HeaderEditorPanel { }
        headerPanel.preferredSize = Dimension(headerPanel.preferredSize.width, 100)
        headerPanel.maximumSize = Dimension(headerPanel.maximumSize.width, 100)
        headerPanel.minimumSize = Dimension(headerPanel.minimumSize.width, 100)

        add(headerPanel, BorderLayout.NORTH)
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (inDrop) {
            g.color = DROP_OVERLAY_COLOR
            g.fillRect(0, 0, width, height)
        }
    }
}