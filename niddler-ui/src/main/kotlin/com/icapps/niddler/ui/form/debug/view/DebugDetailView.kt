package com.icapps.niddler.ui.form.debug.view

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

/**
 * @author nicolaverbeeck
 */
class DebugDetailView : JPanel() {

    private companion object {
        private val DROP_OVERLAY_COLOR = Color(0.75f, 0.42f, 0.0f, 0.3f)
    }

    internal var inDrop = false

    init {
        val handler = DebugDetailDropHandler(this)
        transferHandler = handler
        dropTarget.addDropTargetListener(handler)
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (inDrop) {
            g.color = DROP_OVERLAY_COLOR
            g.fillRect(0, 0, width, height)
        }
    }
}