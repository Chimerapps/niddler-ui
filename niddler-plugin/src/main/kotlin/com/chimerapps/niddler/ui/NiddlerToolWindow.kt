package com.chimerapps.niddler.ui

import com.chimerapps.niddler.ui.actions.SimpleAction
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener

class NiddlerToolWindow(project: Project, disposable: Disposable) : SimpleToolWindowPanel(/* vertical */ false, /* borderless */ true) {

    private val tabsContainer: RunnerLayoutUi
    private var c = 0

    init {
        setupViewActions()

        tabsContainer = RunnerLayoutUi.Factory.getInstance(project).create("niddler-ui", "Detail tabs", "Some session name?", disposable)
        tabsContainer.addListener(object : ContentManagerListener {
            override fun contentAdded(event: ContentManagerEvent?) {
            }

            override fun contentRemoveQuery(event: ContentManagerEvent?) {
            }

            override fun selectionChanged(event: ContentManagerEvent?) {
            }

            override fun contentRemoved(event: ContentManagerEvent) {
                (event.content.component as NiddlerSessionWindow).onClosed()
            }
        }, disposable)

        setContent(tabsContainer.component)

        newSessionWindow()
    }

    private fun newSessionWindow() {
        val content = tabsContainer.createContent("${c++}-contentId", NiddlerSessionWindow(), "Session $c", null, null)
        content.isCloseable = true
        tabsContainer.addContent(content, -1, PlaceInGrid.center, false)
    }

    private fun setupViewActions(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        val newSessionAction = SimpleAction(text = "New session", description = "Start a new session", icon = AllIcons.General.Add) {
            newSessionWindow()
        }
        actionGroup.add(newSessionAction)

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, false)
        setToolbar(toolbar.component)
        return toolbar
    }

}