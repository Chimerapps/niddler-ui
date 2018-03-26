package com.icapps.niddler.ui.form.ui

import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Icon
import javax.swing.JToolBar

/**
 * @author nicolaverbeeck
 */
interface AbstractToolbar {

    val component: Component

    fun addAction(icon: Icon, tooltip: String, actionListener: (Action) -> Unit): Component

    fun addSeparator()

}

class SwingToolbar(orientation: Int = JToolBar.HORIZONTAL) : AbstractToolbar {

    override val component: JToolBar = JToolBar(orientation).apply { isFloatable = false }

    override fun addAction(icon: Icon, tooltip: String, actionListener: (Action) -> Unit): Component {
        return component.add(makeAction(tooltip, icon) { actionListener(it) })
    }

    override fun addSeparator() {
        component.addSeparator()
    }

}

private fun makeAction(toolTip: String, icon: Icon, listener: (event: Action) -> Unit): Action {
    return object : AbstractAction(null, icon) {
        override fun actionPerformed(e: ActionEvent) {
            listener(this)
        }
    }.apply {
        putValue(Action.SHORT_DESCRIPTION, toolTip)
    }
}