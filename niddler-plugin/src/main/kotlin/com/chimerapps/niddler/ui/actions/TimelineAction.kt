package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.component.ViewMode
import com.chimerapps.niddler.ui.util.ui.loadIcon
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware

class TimelineAction(private val window: NiddlerSessionWindow)
    : ToggleAction("Timeline", "View in chronological order", AllIcons.Actions.MoveDown), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean {
        return window.currentViewMode == ViewMode.VIEW_MODE_TIMELINE
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) {
            window.currentViewMode = ViewMode.VIEW_MODE_TIMELINE
        } else {
            window.currentViewModeUnselected()
        }
    }

}