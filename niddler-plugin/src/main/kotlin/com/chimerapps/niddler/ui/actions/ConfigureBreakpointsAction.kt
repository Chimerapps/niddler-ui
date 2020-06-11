package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.intellij.icons.AllIcons

class ConfigureBreakpointsAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = "Configure breakpoints", description = "Configure breakpoints",
        icon = AllIcons.Debugger.MultipleBreakpoints, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}