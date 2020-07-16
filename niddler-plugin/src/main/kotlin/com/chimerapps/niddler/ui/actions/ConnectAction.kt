package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.tr
import com.intellij.icons.AllIcons

class ConnectAction(private val window: NiddlerSessionWindow, listener: () -> Unit)
    : DisableableAction("niddler.action.connect".tr(), "niddler.action.connect.description".tr(), AllIcons.Actions.Execute, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_DISCONNECTED

}