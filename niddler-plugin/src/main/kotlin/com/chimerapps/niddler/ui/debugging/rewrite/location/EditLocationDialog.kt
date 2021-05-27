package com.chimerapps.niddler.ui.debugging.rewrite.location

import com.chimerapps.niddler.ui.util.ext.trimToNull
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocation
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.KeyStroke

class EditLocationDialog(parent: Window?, source: RewriteLocation?) : JDialog(parent, "Edit Location", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, source: RewriteLocation?): RewriteLocation? {
            val dialog = EditLocationDialog(parent, source)
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
    private val contentUI = EditLocationDialogUI(addButtons = true)

    init {
        contentPane = contentUI.content

        contentUI. content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        contentUI. protocolChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        rootPane.defaultButton = contentUI.okButton

        contentUI.okButton.addActionListener {
            result = makeResult()
            dispose()
        }
        contentUI.cancelButton.addActionListener {
            dispose()
        }

        source?.let { initFrom ->
            initFrom.protocol?.let { contentUI.protocolChooser.selectedItem = it }
            initFrom.host?.let { contentUI.host.text = it }
            initFrom.port?.let { contentUI.port.text = it.toString() }
            initFrom.path?.let { contentUI.path.text = it }
            initFrom.query?.let { contentUI.query.text = it }
        }
    }

    private fun makeResult(): RewriteLocation {
        return RewriteLocation(
            protocol = (contentUI.protocolChooser.selectedItem as String).trimToNull(),
            host = contentUI.host.text.trimToNull(),
            path = contentUI.path.text.trimToNull(),
            query = contentUI.query.text.trimToNull(),
            port = contentUI.port.text.trimToNull()
        )
    }
}