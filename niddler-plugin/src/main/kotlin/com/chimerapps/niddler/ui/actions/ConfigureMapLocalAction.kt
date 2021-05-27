package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.intellij.icons.AllIcons

class ConfigureMapLocalAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = "Configure local mapping", description = "Configure local mapping",
        icon = IncludedIcons.Action.localMapping, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}