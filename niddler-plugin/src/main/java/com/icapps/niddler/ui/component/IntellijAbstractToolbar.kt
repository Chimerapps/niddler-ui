package com.icapps.niddler.ui.component

import com.icapps.niddler.ui.form.ui.AbstractToolbar
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import java.awt.Component
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author nicolaverbeeck
 */
class IntellijAbstractToolbar(horizontal: Boolean) : AbstractToolbar {

    internal val internal: ActionToolbar
    override val component: JComponent
        get() = internal.component

    private val actionGroup: DefaultActionGroup

    init {
        actionGroup = DefaultActionGroup()

        internal = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, horizontal)
        internal.updateActionsImmediately()
    }

    override fun addAction(icon: Icon, tooltip: String, actionListener: (Component?) -> Unit): Component {
        actionGroup.addAction(DefaultAction(tooltip, icon, actionListener))
    }

    override fun addSeparator() {
        actionGroup.addSeparator()
    }

}

private class DefaultAction(toolTip: String,
                            defaultIcon: Icon,
                            private val actionListener: (Component?) -> Unit)
    : DumbAwareAction(toolTip, toolTip, defaultIcon) {

    override fun actionPerformed(e: AnActionEvent?) {
        actionListener(null)
    }

}
