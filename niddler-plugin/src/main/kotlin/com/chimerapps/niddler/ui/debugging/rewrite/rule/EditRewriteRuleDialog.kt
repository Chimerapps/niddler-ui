package com.chimerapps.niddler.ui.debugging.rewrite.rule

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import java.awt.Dimension
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.KeyStroke

@Suppress("DuplicatedCode")
class EditRewriteRuleDialog(parent: Window?,
                            source: RewriteRule?)
    : EditRewriteRuleDialogUI(parent) {

    companion object {
        fun show(parent: Window?, source: RewriteRule?): RewriteRule? {
            val dialog = EditRewriteRuleDialog(parent, source)
            dialog.pack()
            dialog.setSize(520, dialog.height)
            dialog.maximumSize = Dimension(520, dialog.height)
            dialog.minimumSize = Dimension(520, dialog.height)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.result
        }
    }

    var result: RewriteRule? = null
        private set

    init {
        okButton.addActionListener {
            //TODO
            dispose()
        }
        cancelButton.addActionListener {
            dispose()
        }

        content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        typeChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    }

}