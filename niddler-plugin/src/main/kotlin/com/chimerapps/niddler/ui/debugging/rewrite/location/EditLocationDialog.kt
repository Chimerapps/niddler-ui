package com.chimerapps.niddler.ui.debugging.rewrite.location

import com.chimerapps.niddler.ui.util.ext.trimToNull
import com.icapps.niddler.lib.debugger.model.configuration.DebugLocation
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.KeyStroke


class EditLocationDialog(parent: Window?, source: DebugLocation?) : EditLocationDialogUI(parent) {

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

    var result: DebugLocation? = null
        private set

    init {
        content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        protocolChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        rootPane.defaultButton = okButton

        okButton.addActionListener {
            result = makeResult()
            dispose()
        }
        cancelButton.addActionListener {
            dispose()
        }

        source?.let { initFrom ->
            initFrom.protocol?.let { protocolChooser.selectedItem = it }
            initFrom.host?.let { host.text = it }
            initFrom.port?.let { port.text = it.toString() }
            initFrom.path?.let { path.text = it }
            initFrom.query?.let { query.text = it }
        }
    }

    private fun makeResult(): DebugLocation {
        return DebugLocation(protocol = (protocolChooser.selectedItem as String).trimToNull(),
                host = host.text.trimToNull(),
                path = path.text.trimToNull(),
                query = query.text.trimToNull(),
                port = port.text.trimToNull())
    }
}