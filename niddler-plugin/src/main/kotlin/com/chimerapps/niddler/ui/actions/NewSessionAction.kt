package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.intellij.icons.AllIcons

class NewSessionAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = "New session", description = "Start a new session", icon = AllIcons.General.Add, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}