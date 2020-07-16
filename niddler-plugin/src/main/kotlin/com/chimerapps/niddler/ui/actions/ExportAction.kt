package com.chimerapps.niddler.ui.actions

import com.chimerapps.niddler.ui.util.tr
import com.intellij.icons.AllIcons

class ExportAction(actionListener: () -> Unit) : DisableableAction("niddler.action.export".tr(), "niddler.action.export.description".tr(),
        AllIcons.Actions.Menu_saveall, actionListener) {

    override var isEnabled: Boolean = false

}