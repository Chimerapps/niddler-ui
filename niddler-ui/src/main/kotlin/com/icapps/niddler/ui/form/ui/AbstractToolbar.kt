package com.icapps.niddler.ui.form.ui

import java.awt.Component
import java.awt.Dimension
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JToolBar

/**
 * @author nicolaverbeeck
 */
interface AbstractToolbar {

    val component: Component

    fun addAction(icon: Icon, tooltip: String, actionListener: (Component) -> Unit): Component

    fun addSeparator()

}

class SwingToolbar(orientation: Int = JToolBar.HORIZONTAL, border: Boolean = false) : AbstractToolbar {

    override val component: JToolBar = JToolBar(orientation).apply {
        isFloatable = false
        margin = Insets(0, 4, 0, 4)
        if (border)
            this.border = BorderFactory.createLoweredBevelBorder()
    }

    override fun addAction(icon: Icon, tooltip: String, actionListener: (Component) -> Unit): Component {
        val button = makeAction(tooltip, icon, actionListener)
        component.add(button)
        return component
    }

    override fun addSeparator() {
        component.addSeparator()
    }

}

fun makeAction(toolTip: String, icon: Icon, listener: (Component) -> Unit): JButton {
    return JButton().apply {
        this.icon = icon
        text = ""
        toolTipText = toolTip
        maximumSize = Dimension(32, 32)
        minimumSize = Dimension(32, 32)
        preferredSize = Dimension(32, 32)
        addActionListener { listener(this) }
    }
}