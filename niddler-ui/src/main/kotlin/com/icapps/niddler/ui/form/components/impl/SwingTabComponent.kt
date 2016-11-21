package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.ui.form.components.TabComponent
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTabbedPane

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class SwingTabComponent : JTabbedPane(), TabComponent {

    override val asComponent: Component
        get() = this

    override fun addTab(title: String, component: JComponent) {
        super.addTab(title, component)
    }

    override val numTabs: Int
        get() = tabCount

    override fun get(index: Int): Component {
        return getComponentAt(index)
    }

}