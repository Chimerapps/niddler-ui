package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.localization.Tr
import com.intellij.icons.AllIcons

class ConnectAction(private val window: NiddlerSessionWindow, listener: () -> Unit)
    : DisableableAction(Tr.ActionConnect.tr(), Tr.ActionConnectDescription.tr(), AllIcons.Actions.Execute, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_DISCONNECTED

}