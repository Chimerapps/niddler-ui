package com.icapps.niddler.ui.component

import com.icapps.niddler.ui.form.ui.AbstractAction
import com.icapps.niddler.ui.form.ui.AbstractToolbar
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
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

    override fun addAction(icon: Icon, tooltip: String, actionListener: (AbstractAction) -> Unit): AbstractAction {
        val action = DefaultAction(internal, tooltip, icon, actionListener)
        actionGroup.addAction(action)
        return action
    }

    override fun addSeparator() {
        actionGroup.addSeparator()
    }

}

private class DefaultAction(private val toolbar: ActionToolbar,
                            toolTip: String,
                            defaultIcon: Icon,
                            private val actionListener: (AbstractAction) -> Unit)
    : DumbAwareAction(toolTip, toolTip, defaultIcon), AbstractAction {

    override var isEnabled: Boolean = true

    override fun actionPerformed(e: AnActionEvent?) {
        actionListener(this)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = isEnabled
    }
}
