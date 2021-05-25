package com.chimerapps.niddler.ui.debugging.maplocal

import com.chimerapps.niddler.ui.debugging.rewrite.location.EditLocationDialogUI
import com.chimerapps.niddler.ui.util.ext.trimToNull
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocation
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.KeyStroke

/**
 * @author Nicola Verbeeck
 */
class EditLocalMappingDialog(parent: Window?, source: RewriteLocation?) : JDialog(parent, "Edit Mapping", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, source: RewriteLocation?): RewriteLocation? {
            val dialog = EditLocalMappingDialog(parent, source)
            dialog.pack()
            dialog.setSize(420, dialog.height)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.result
        }
    }

    var result: RewriteLocation? = null
        private set

    private val content = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
    }
    private val editLocationUI = EditLocationDialogUI(addButtons = false)

    init {
        content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        editLocationUI.protocolChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        rootPane.defaultButton = editLocationUI.okButton

        editLocationUI.okButton.addActionListener {
            result = makeResult()
            dispose()
        }
        editLocationUI.cancelButton.addActionListener {
            dispose()
        }

        source?.let { initFrom ->
            initFrom.protocol?.let { editLocationUI.protocolChooser.selectedItem = it }
            initFrom.host?.let { editLocationUI.host.text = it }
            initFrom.port?.let { editLocationUI.port.text = it.toString() }
            initFrom.path?.let { editLocationUI.path.text = it }
            initFrom.query?.let { editLocationUI.query.text = it }
        }

        val locationPanel = object : JPanel(BorderLayout()) {
            override fun getPreferredSize(): Dimension = Dimension(this.parent.width, super.getPreferredSize().height)
        }.also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        }
        locationPanel.border = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Map From")
        locationPanel.add(BackgroundRenderingJPanel(Color(0x11000000, true)).also {
            it.add(editLocationUI.content)
            editLocationUI.content.background = Color(0, true)
        }, BorderLayout.NORTH)

        content.add(locationPanel)
        contentPane = content
    }

    private fun makeResult(): RewriteLocation {
        return RewriteLocation(
            protocol = (editLocationUI.protocolChooser.selectedItem as String).trimToNull(),
            host = editLocationUI.host.text.trimToNull(),
            path = editLocationUI.path.text.trimToNull(),
            query = editLocationUI.query.text.trimToNull(),
            port = editLocationUI.port.text.trimToNull()
        )
    }
}

class BackgroundRenderingJPanel(val color: Color) : JPanel() {
    init {
        isOpaque = false
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        g.color = color
        (g as? Graphics2D)?.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        g.fillRoundRect(0, 0, width - 1, height - 1, 20, 20)

    }

}