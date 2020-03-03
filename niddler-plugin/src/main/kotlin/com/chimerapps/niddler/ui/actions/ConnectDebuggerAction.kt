package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.intellij.icons.AllIcons

class ConnectDebuggerAction(private val window: NiddlerSessionWindow, listener: () -> Unit)
    : DisableableAction("Connect with debugger",
        "Connect to niddler server using the debugger", AllIcons.Actions.StartDebugger, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_DISCONNECTED

}