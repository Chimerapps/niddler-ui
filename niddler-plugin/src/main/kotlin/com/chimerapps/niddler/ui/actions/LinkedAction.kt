package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.component.ViewMode
import com.chimerapps.niddler.ui.util.localization.Tr
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware

class LinkedAction(private val window: NiddlerSessionWindow)
    : ToggleAction(Tr.ActionViewLinked.tr(), Tr.ActionViewLinkedDescription.tr(), AllIcons.Actions.SyncPanels), DumbAware {

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