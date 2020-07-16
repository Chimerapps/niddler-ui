package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.util.localization.Tr
import com.chimerapps.niddler.ui.util.ui.IncludedIcons

class ConfigureRewriteAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = Tr.ActionConfigureRewrite.tr(), description = Tr.ActionConfigureRewriteDescription.tr(),
        icon = IncludedIcons.Action.amend, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}