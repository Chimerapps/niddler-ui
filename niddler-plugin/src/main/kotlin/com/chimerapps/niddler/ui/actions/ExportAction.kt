package com.chimerapps.niddler.ui.actions

import com.intellij.icons.AllIcons

class ExportAction(actionListener: () -> Unit) : DisableableAction("Export", "Export messages to HAR", AllIcons.Actions.Menu_saveall, actionListener) {

    override var isEnabled: Boolean = false

}