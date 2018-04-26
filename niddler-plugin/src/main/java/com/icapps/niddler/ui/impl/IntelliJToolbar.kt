package com.icapps.niddler.ui.impl

import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.util.loadIcon
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.SimpleToolWindowPanel
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 16/11/2017.
 */
class IntelliJToolbar(panel: SimpleToolWindowPanel) : NiddlerMainToolbar {

    override var listener: NiddlerMainToolbar.ToolbarListener? = null

    internal var mode: Int = 0
    internal var breakpointsMuted = false

    internal val internal: ActionToolbar
    val component: JComponent
        get() = internal.component

    init {
        val group = DefaultActionGroup()

        val chronologicalIcon = loadIcon("/ic_chronological.png")
        val chronologicalIconSelected = loadIcon("/ic_chronological_selected.png")

        val linkedIcon = loadIcon("/ic_link.png")
        val linkedIconSelected = loadIcon("/ic_link.png")

        group.add(TimelineAction(this, chronologicalIcon, chronologicalIconSelected))
        group.add(LinkedAction(this, linkedIcon, linkedIconSelected))

        group.addSeparator()
        group.add(ClearAction(this))
        group.addSeparator()

        val muteBreakpointsIcon = loadIcon("/muteBreakpoints.png")
        val mutedBreakpointsIcon = loadIcon("/muteBreakpoints_muted.png")
        group.add(ConfigureBreakpointsAction(this))
        group.add(MuteBreakpointsAction(this, muteBreakpointsIcon, mutedBreakpointsIcon))

        group.addSeparator()
        group.add(ExportAction(this))

        internal = ActionManager.getInstance().createActionToolbar("Niddler", group, false)
        internal.setTargetComponent(panel)
        internal.updateActionsImmediately()
    }

    override fun onBreakpointsMuted(muted: Boolean) {
        breakpointsMuted = muted
        internal.updateActionsImmediately()
    }
}

private class TimelineAction(private val toolbar: IntelliJToolbar,
                             private val defaultIcon: Icon,
                             private val selectedIcon: Icon) : DumbAwareAction("Chronological",
        "View messages in chronological order, showing them in order which they occurred", selectedIcon) {

    private var lastMode = 0

    override fun update(e: AnActionEvent) {
        if (toolbar.mode == lastMode)
            return

        if (toolbar.mode == 0) {
            e.presentation.icon = selectedIcon
        } else {
            e.presentation.icon = defaultIcon
        }
        lastMode = toolbar.mode
    }

    override fun actionPerformed(e: AnActionEvent) {
        toolbar.listener?.onTimelineSelected()
        toolbar.mode = 0
        toolbar.internal.updateActionsImmediately()
    }
}

private class LinkedAction(private val toolbar: IntelliJToolbar,
                           private val defaultIcon: Icon,
                           private val selectedIcon: Icon) : DumbAwareAction("Linked mode",
        "View messages in linked mode. Showing the request and response grouped together. When supported, this mode will also show the actual network requests", defaultIcon) {

    private var lastMode = 1

    override fun update(e: AnActionEvent) {
        if (toolbar.mode == lastMode)
            return

        if (toolbar.mode == 1) {
            e.presentation.icon = selectedIcon
        } else {
            e.presentation.icon = defaultIcon
        }
        lastMode = toolbar.mode
    }

    override fun actionPerformed(e: AnActionEvent) {
        toolbar.listener?.onLinkedSelected()
        toolbar.mode = 1
        toolbar.internal.updateActionsImmediately()
    }
}


private class ExportAction(private val toolbar: IntelliJToolbar) : DumbAwareAction("Export", "Export the session",
        ExportAction::class.java.loadIcon("/ic_save.png")) {

    override fun actionPerformed(e: AnActionEvent?) {
        toolbar.listener?.onExportSelected()
    }

}

private class ClearAction(private val toolbar: IntelliJToolbar) : DumbAwareAction("Clear", "Clear current session",
        ClearAction::class.java.loadIcon("/ic_delete.png")) {

    override fun actionPerformed(e: AnActionEvent?) {
        toolbar.listener?.onClearSelected()
    }

}

private class ConfigureBreakpointsAction(private val toolbar: IntelliJToolbar) :
        DumbAwareAction("Configure debugger", "Configure debugger",
                ConfigureBreakpointsAction::class.java.loadIcon("/viewBreakpoints.png")) {

    override fun actionPerformed(e: AnActionEvent?) {
        toolbar.listener?.onConfigureBreakpointsSelected()
    }
}

private class MuteBreakpointsAction(private val toolbar: IntelliJToolbar,
                                    private val defaultIcon: Icon,
                                    private val mutedIcon: Icon) : DumbAwareAction("Mute breakpoints",
        "Mute/unmute breakpoints", defaultIcon) {

    private var lastMode = true

    override fun update(e: AnActionEvent) {
        if (toolbar.breakpointsMuted == lastMode)
            return

        if (toolbar.breakpointsMuted) {
            e.presentation.icon = mutedIcon
        } else {
            e.presentation.icon = defaultIcon
        }
        lastMode = toolbar.breakpointsMuted
    }

    override fun actionPerformed(e: AnActionEvent) {
        toolbar.listener?.onMuteBreakpointsSelected()
    }
}
