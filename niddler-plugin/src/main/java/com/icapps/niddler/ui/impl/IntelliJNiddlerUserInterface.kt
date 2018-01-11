package com.icapps.niddler.ui.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.impl.SwingNiddlerUserInterface
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.FilterComponent
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 * @date 16/11/2017.
 */
class IntelliJNiddlerUserInterface(componentsFactory: ComponentsFactory) : SwingNiddlerUserInterface(componentsFactory) {

    override val asComponent: JComponent
        get() = toolWindowPanel.component

    val toolWindowPanel: SimpleToolWindowPanel

    init {
        toolWindowPanel = SimpleToolWindowPanel(false, true)
        toolWindowPanel.setContent(rootPanel)
    }

    override fun initToolbar() {
        val toolbar = IntelliJToolbar(toolWindowPanel)
        this.toolbar = toolbar
        toolWindowPanel.setToolbar(toolbar.component)
    }

    override fun initFilter(parent: JPanel) {
        val filter = object : FilterComponent("niddler-filter", 10, true) {
            override fun filter() {
                filterListener?.invoke(filter)
            }
        }
        filter.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)

        parent.add(filter, BorderLayout.EAST)
    }

}