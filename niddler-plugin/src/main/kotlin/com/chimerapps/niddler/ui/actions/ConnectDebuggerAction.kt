package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.tr
import com.intellij.icons.AllIcons

class ConnectDebuggerAction(private val window: NiddlerSessionWindow, listener: () -> Unit)
    : DisableableAction("niddler.action.connect.debugger".tr(),
        "niddler.action.connect.debugger.description".tr(), AllIcons.Actions.StartDebugger, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_DISCONNECTED

}