package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.localization.Tr
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware

class ScrollToBottomAction(private val window: NiddlerSessionWindow)
    : ToggleAction(Tr.ActionScrollToEnd.tr(), Tr.ActionScrollToEndDescription.tr(), AllIcons.RunConfigurations.Scroll_down), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean {
        return window.scrollToEnd
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        window.scrollToEnd = state
    }
}