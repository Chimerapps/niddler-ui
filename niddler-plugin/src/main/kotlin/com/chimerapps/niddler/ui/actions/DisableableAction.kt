package com.chimerapps.niddler.ui.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.Icon

abstract class DisableableAction(text: String?, description: String?, icon: Icon, actionListener: () -> Unit) : SimpleAction(text, description, icon, actionListener) {

    abstract val isEnabled: Boolean

    fun updateState() {
        templatePresentation.isEnabled = isEnabled
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = isEnabled
    }

    override fun actionPerformed(e: AnActionEvent?) {
        if (isEnabled)
            actionListener()
    }

}