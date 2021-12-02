package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.intellij.openapi.actionSystem.AnActionEvent

class ToggleWifiAction(private val window: NiddlerSessionWindow, actionListener: () -> Unit)
    : DisableableAction(text = "Toggle internet", description = "Toggle internet on device",
        icon = IncludedIcons.Action.enableWifi, actionListener = actionListener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_CONNECTED && window.debuggerService != null

    override fun update(e: AnActionEvent) {
        e.presentation.icon = when {
            !isEnabled ->  IncludedIcons.Action.disableWifi
            window.debuggerService?.isWifiDisabled == true -> IncludedIcons.Action.enableWifi
            else -> IncludedIcons.Action.disableWifi
        }
        super.update(e)
    }
}