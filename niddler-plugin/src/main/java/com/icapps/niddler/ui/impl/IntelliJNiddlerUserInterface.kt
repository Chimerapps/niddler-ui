package com.icapps.niddler.ui.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.impl.SwingNiddlerUserInterface
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import javax.swing.JComponent

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

}