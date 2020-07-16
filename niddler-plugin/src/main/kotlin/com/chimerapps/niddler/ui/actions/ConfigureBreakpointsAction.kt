package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.util.tr
import com.intellij.icons.AllIcons

class ConfigureBreakpointsAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = "niddler.action.configure.breakpoints".tr(), description = "niddler.action.configure.breakpoints.description".tr(),
        icon = AllIcons.Debugger.MultipleBreakpoints, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}