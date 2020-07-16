package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.util.tr
import com.chimerapps.niddler.ui.util.ui.IncludedIcons

class ConfigureRewriteAction(private val window: NiddlerToolWindow, actionListener: () -> Unit)
    : DisableableAction(text = "niddler.action.configure.rewrite".tr(), description = "niddler.action.configure.rewrite.description".tr(),
        icon = IncludedIcons.Action.amend, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.isReady

}