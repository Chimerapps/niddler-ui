package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.util.localization.Tr
import com.intellij.icons.AllIcons

class NewSessionAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = Tr.ActionNewSession.tr(), description = Tr.ActionNewSessionDescription.tr(),
        icon = AllIcons.General.Add, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}