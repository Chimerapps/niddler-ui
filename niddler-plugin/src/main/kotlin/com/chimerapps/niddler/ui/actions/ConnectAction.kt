package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.intellij.icons.AllIcons

class ConnectAction(private val window: NiddlerSessionWindow, listener: () -> Unit)
    : DisableableAction("Connect", "Connect to niddler server", AllIcons.Actions.Execute, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_DISCONNECTED

}