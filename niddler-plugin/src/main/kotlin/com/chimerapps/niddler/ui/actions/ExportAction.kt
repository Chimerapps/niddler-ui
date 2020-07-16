package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.util.localization.Tr
import com.intellij.icons.AllIcons

class ExportAction(actionListener: () -> Unit) : DisableableAction(Tr.ActionExport.tr(), Tr.ActionExportDescription.tr(), AllIcons.Actions.Menu_saveall, actionListener) {

    override var isEnabled: Boolean = false

}