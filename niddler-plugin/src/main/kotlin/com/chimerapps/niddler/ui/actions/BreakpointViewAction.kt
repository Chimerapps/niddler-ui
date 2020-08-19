package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.component.ViewMode
import com.chimerapps.niddler.ui.util.localization.Tr
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware

class BreakpointViewAction(private val window: NiddlerSessionWindow)
    : ToggleAction(Tr.ActionViewLinked.tr(), Tr.ActionViewLinkedDescription.tr(), AllIcons.Toolwindows.ToolWindowDebugger), DumbAware {

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.icon = if (window.hasItemInBreakpoint) {
            ExecutionUtil.getLiveIndicator(AllIcons.Toolwindows.ToolWindowDebugger)
        } else {
            AllIcons.Toolwindows.ToolWindowDebugger
        }
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return window.currentViewMode == ViewMode.BREAKPOINT
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) {
            window.currentViewMode = ViewMode.BREAKPOINT
        } else {
            window.currentViewModeUnselected()
        }
    }

}