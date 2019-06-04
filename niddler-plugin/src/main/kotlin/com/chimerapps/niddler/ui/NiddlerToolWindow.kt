package com.chimerapps.niddler.ui

import com.chimerapps.niddler.ui.actions.LinkedAction
import com.chimerapps.niddler.ui.actions.TimelineAction
import com.chimerapps.niddler.ui.util.ui.loadIcon
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel

class NiddlerToolWindow : SimpleToolWindowPanel(/* vertical */ false, /* borderless */ true) {

    private val updateableActions = mutableListOf<AnAction>()
    var currentViewMode: ViewMode = ViewMode.VIEW_MODE_TIMELINE
        set(value) {
            field = value
            updateActions()
        }

    init {
        setupViewActions()
    }

    fun currentViewModeUnselected() {
        when (currentViewMode) {
            ViewMode.VIEW_MODE_TIMELINE -> currentViewMode = ViewMode.VIEW_MODE_LINKED
            ViewMode.VIEW_MODE_LINKED -> currentViewMode = ViewMode.VIEW_MODE_TIMELINE
        }
    }

    private fun setupViewActions() {
        val actionGroup = DefaultActionGroup()

        val timelineAction = TimelineAction(this, text = "Timeline", description = "View in chronological order", icon = loadIcon("/ic_chronological.png"))
        updateableActions += timelineAction
        actionGroup.add(timelineAction)

        val linkedAction = LinkedAction(this, text = "Linked", description = "View request and responses grouped together", icon = loadIcon("/ic_linked.png"))
        updateableActions += linkedAction
        actionGroup.add(linkedAction)

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, false)
        setToolbar(toolbar.component)
    }

    private fun updateActions() {
        updateableActions.forEach { action ->
            val event = AnActionEvent.createFromAnAction(action, null, "Niddler", DataManager.getInstance().getDataContext(component))
            action.update(event)
        }
    }
}

enum class ViewMode {
    VIEW_MODE_TIMELINE,
    VIEW_MODE_LINKED
}