package com.chimerapps.niddler.ui.debugging.rewrite.rule

import com.chimerapps.niddler.ui.util.ext.trimToNull
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import java.awt.Dimension
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.KeyStroke

@Suppress("DuplicatedCode")
class EditRewriteRuleDialog(parent: Window?,
                            private val source: RewriteRule?)
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
            result = makeResult()
            if (result != null)
                dispose()
            else {
                //TODO show error
            }
        }
        cancelButton.addActionListener {
            dispose()
        }

        content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        typeChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        if (source != null) {
            val overrideRequestChecked = (source.ruleType == RewriteType.HOST ||
                    source.ruleType == RewriteType.PATH ||
                    source.ruleType == RewriteType.URL ||
                    source.ruleType == RewriteType.ADD_QUERY_PARAM ||
                    source.ruleType == RewriteType.MODIFY_QUERY_PARAM ||
                    source.ruleType == RewriteType.REMOVE_QUERY_PARAM)
            val overrideResponseChecked = (source.ruleType == RewriteType.RESPONSE_STATUS)

            typeChooser.selectedItem = source.ruleType
            requestCheckbox.isSelected = overrideRequestChecked || source.matchRequest
            responseCheckbox.isSelected = overrideResponseChecked || source.matchResponse
            matchNameText.text = source.matchHeader ?: ""
            matchValueText.text = source.matchValue ?: ""
            matchNameRegex.isSelected = source.matchHeaderRegex
            matchValueRegex.isSelected = source.matchValueRegex

            replaceNameText.text = source.newHeader ?: ""
            replaceValueText.text = source.newValue ?: ""

            if (source.replaceType == ReplaceType.REPLACE_FIRST) {
                replaceFirst.isSelected = true
            } else {
                replaceAll.isSelected = true
            }
        } else {
            requestCheckbox.isSelected = true
            replaceAll.isSelected = true
        }
    }

    private fun makeResult(): RewriteRule? {
        val type = typeChooser.selectedItem as RewriteType? ?: return null

        //Headers + query have the matchHeader fields set

        var matchHeaderRegex = false
        val matchValueRegex = this.matchValueRegex.isSelected
        val newHeaderRegex = false
        val newValueRegex = false //These seem false all the time in charles?!
        var matchHeader: String? = null
        val matchValue: String? = matchValueText.text.trimToNull()
        var newHeader: String? = null
        val newValue: String? = replaceValueText.text.trimToNull()

        when (type) {
            RewriteType.ADD_HEADER,
            RewriteType.MODIFY_HEADER,
            RewriteType.REMOVE_HEADER,
            RewriteType.ADD_QUERY_PARAM,
            RewriteType.MODIFY_QUERY_PARAM,
            RewriteType.REMOVE_QUERY_PARAM -> {
                matchHeaderRegex = matchNameRegex.isSelected
                matchHeader = matchNameText.text.trimToNull()
                newHeader = replaceNameText.text.trimToNull()
            }
            else -> {
                //Don't check non-header stuff
            }
        }

        return RewriteRule(active = source?.active ?: true,
                ruleType = type,
                matchHeaderRegex = matchHeaderRegex,
                matchValueRegex = matchValueRegex,
                matchRequest = requestCheckbox.isSelected,
                matchResponse = responseCheckbox.isSelected,
                newHeaderRegex = newHeaderRegex,
                newValueRegex = newValueRegex,
                matchWholeValue = matchEntire.isSelected,
                caseSensitive = caseSensitive.isSelected,
                replaceType = if (replaceFirst.isSelected) ReplaceType.REPLACE_FIRST else ReplaceType.REPLACE_ALL,
                matchHeader = matchHeader,
                matchValue = matchValue,
                newHeader = newHeader,
                newValue = newValue)
    }
}