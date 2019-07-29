package com.chimerapps.niddler.ui.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import javax.swing.Icon

open class SimpleAction(text: String?, description: String?, icon: Icon, protected val actionListener: () -> Unit) : DumbAwareAction(text, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        actionListener()
    }

}