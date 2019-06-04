package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.ViewMode
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

class LinkedAction(private val window: NiddlerToolWindow, text: String?, description: String?, icon: Icon) : ToggleAction(text, description, icon), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean {
        return window.currentViewMode == ViewMode.VIEW_MODE_LINKED
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) {
            window.currentViewMode = ViewMode.VIEW_MODE_LINKED
        } else {
            window.currentViewModeUnselected()
        }
    }

}