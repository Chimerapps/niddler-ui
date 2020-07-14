package com.chimerapps.niddler.ui.debugging.rewrite.location

import com.chimerapps.niddler.ui.util.ext.trimToNull
import com.icapps.niddler.lib.debugger.model.configuration.DebugLocation
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.KeyStroke


class EditLocationDialog(parent: Window?, source: DebugLocation?) : JDialog(parent, "Edit Location", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, source: DebugLocation?): DebugLocation? {
            val dialog = EditLocationDialog(parent, source)
            dialog.pack()
            dialog.setSize(420, dialog.height)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.result
        }
    }

    private val ui = EditLocationUI(includeAction = false, includeButtons = true)

    var result: DebugLocation? = null
        private set

    init {
        contentPane = ui.content
        ui.content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        ui.protocolChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        rootPane.defaultButton = ui.okButton

        ui.okButton.addActionListener {
            result = makeResult()
            dispose()
        }
        ui.cancelButton.addActionListener {
            dispose()
        }

        source?.let { initFrom ->
            initFrom.protocol?.let { ui.protocolChooser.selectedItem = it }
            initFrom.host?.let { ui.host.text = it }
            initFrom.port?.let { ui.port.text = it.toString() }
            initFrom.path?.let { ui.path.text = it }
            initFrom.query?.let { ui.query.text = it }
        }
    }

    private fun makeResult(): DebugLocation {
        return DebugLocation(protocol = (ui.protocolChooser.selectedItem as String).trimToNull(),
                host = ui.host.text.trimToNull(),
                path = ui.path.text.trimToNull(),
                query = ui.query.text.trimToNull(),
                port = ui.port.text.trimToNull())
    }
}