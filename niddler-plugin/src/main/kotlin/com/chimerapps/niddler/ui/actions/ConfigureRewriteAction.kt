package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.intellij.icons.AllIcons

class ConfigureRewriteAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = "Configure rewrite rules", description = "Configure rewrite rules",
        icon = AllIcons.Debugger.MultipleBreakpoints, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}