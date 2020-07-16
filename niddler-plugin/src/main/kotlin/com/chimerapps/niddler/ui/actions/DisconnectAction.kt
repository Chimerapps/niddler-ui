package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.tr
import com.intellij.icons.AllIcons

class DisconnectAction(private val window: NiddlerSessionWindow, listener: () -> Unit)
    : DisableableAction("niddler.action.disconnect".tr(), "niddler.action.disconnect.description".tr(), AllIcons.Actions.Suspend, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_CONNECTED

}