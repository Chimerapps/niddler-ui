package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.tr
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware

class ScrollToBottomAction(private val window: NiddlerSessionWindow)
    : ToggleAction("niddler.action.scroll.to.end".tr(), "niddler.action.scroll.to.end.description".tr(), AllIcons.RunConfigurations.Scroll_down), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean {
        return window.scrollToEnd
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        window.scrollToEnd = state
    }
}