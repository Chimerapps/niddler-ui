package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.intellij.icons.AllIcons

class DisconnectAction(private val window: NiddlerSessionWindow, listener: () -> Unit)
    : DisableableAction("Disconnect", "Disconnect from niddler server", AllIcons.Actions.Suspend, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_CONNECTED

}