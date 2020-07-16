package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.util.localization.Tr
import com.intellij.icons.AllIcons

class ConfigureBreakpointsAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = Tr.ActionConfigureBreakpoints.tr(), description = Tr.ActionConfigureBreakpointsDescription.tr(),
        icon = AllIcons.Debugger.MultipleBreakpoints, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}