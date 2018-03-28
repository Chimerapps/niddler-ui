package com.icapps.niddler.ui.component

import com.icapps.niddler.ui.form.components.TabComponent
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.ui.content.impl.ContentImpl
import java.awt.Component
import javax.swing.JComponent


/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class IntelliJTabComponent(project: Project?,
                           parent: Disposable) : TabComponent {

    private val layoutUI: RunnerLayoutUi
    private val titles: MutableList<String> = arrayListOf()

    init {
        layoutUI = RunnerLayoutUi.Factory.getInstance(project).create("niddler-ui", "Detail tabs", "Some session name?", parent)
    }

    override val asComponent: Component
        get() = layoutUI.component

    override fun addTab(title: String, component: JComponent) {
        addTab(layoutUI, component, title, PlaceInGrid.center)
        titles.add(title)
    }

    override val numTabs: Int
        get() = titles.size

    override fun get(index: Int): Component {
        return (layoutUI.findContent( "${titles[index]}-contentId") as ContentImpl).component
    }

    private fun addTab(layoutUi: RunnerLayoutUi, component: JComponent, name: String, defaultPlace: PlaceInGrid): Content {
        val content = layoutUi.createContent("$name-contentId", component, name, null, null)
        content.isCloseable = false
        layoutUi.addContent(content, -1, defaultPlace, false)
        return content
    }

    override fun invalidate() {
        asComponent.invalidate()
    }

    override fun repaint() {
        asComponent.repaint()
    }
}