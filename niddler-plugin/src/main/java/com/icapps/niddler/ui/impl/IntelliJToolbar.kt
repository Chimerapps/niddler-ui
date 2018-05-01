package com.icapps.niddler.ui.impl

import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.impl.IntelliJToolbar.Companion.MODE_DEBUG
import com.icapps.niddler.ui.impl.IntelliJToolbar.Companion.MODE_LINKED
import com.icapps.niddler.ui.impl.IntelliJToolbar.Companion.MODE_TIMELINE
import com.icapps.niddler.ui.util.loadIcon
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 16/11/2017.
 */
class IntelliJToolbar(panel: SimpleToolWindowPanel) : NiddlerMainToolbar {

    companion object {
        const val MODE_TIMELINE = 0
        const val MODE_LINKED = 1
        const val MODE_DEBUG = 2
    }

    override var listener: NiddlerMainToolbar.ToolbarListener? = null

    internal var mode: Int = MODE_TIMELINE
    internal var breakpointsMuted = false

    override var hasWaitingBreakpoint: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                internal.updateActionsImmediately()
            }
        }

    internal val internal: ActionToolbar
    val component: JComponent
        get() = internal.component

    init {
        val group = DefaultActionGroup()

        val chronologicalIcon = loadIcon("/ic_chronological.png")
        val linkedIcon = loadIcon("/ic_link.png")
        val debugViewIcon = loadIcon("/ic_debug_active.png")
        val debugWarningView = loadIcon("/ic_debug_active_warning.png")

        group.add(TimelineAction(this, chronologicalIcon))
        group.add(LinkedAction(this, linkedIcon))
        group.add(DebugViewAction(this, debugViewIcon, debugWarningView))

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
                             private val defaultIcon: Icon) : ToggleAction("Chronological",
        "View messages in chronological order, showing them in order which they occurred", defaultIcon),
        DumbAware {

    override fun isSelected(e: AnActionEvent?): Boolean {
        return toolbar.mode == MODE_TIMELINE
    }

    override fun setSelected(e: AnActionEvent?, state: Boolean) {
        if (state) {
            toolbar.listener?.onTimelineSelected()
            toolbar.mode = MODE_TIMELINE
            toolbar.internal.updateActionsImmediately()
        }
    }
}

private class LinkedAction(private val toolbar: IntelliJToolbar,
                           private val defaultIcon: Icon) : ToggleAction("Linked mode",
        "View messages in linked mode. Showing the request and response grouped together. When supported, this mode will also show the actual network requests", defaultIcon),
        DumbAware {

    override fun isSelected(e: AnActionEvent?): Boolean {
        return toolbar.mode == MODE_LINKED
    }

    override fun setSelected(e: AnActionEvent?, state: Boolean) {
        if (state) {
            toolbar.listener?.onLinkedSelected()
            toolbar.mode = MODE_LINKED
            toolbar.internal.updateActionsImmediately()
        }
    }
}

private class DebugViewAction(private val toolbar: IntelliJToolbar,
                              private val defaultIcon: Icon,
                              private val warningIcon: Icon) : ToggleAction("Debug view",
        "Show debugger view", defaultIcon), DumbAware {

    private var lastWarningStatus = false

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (toolbar.hasWaitingBreakpoint == lastWarningStatus)
            return

        lastWarningStatus = toolbar.hasWaitingBreakpoint
        if (lastWarningStatus)
            e.presentation.icon = warningIcon
        else
            e.presentation.icon = defaultIcon
    }

    override fun isSelected(e: AnActionEvent?): Boolean {
        return toolbar.mode == MODE_DEBUG
    }

    override fun setSelected(e: AnActionEvent?, state: Boolean) {
        if (state) {
            toolbar.listener?.onLinkedSelected()
            toolbar.mode = MODE_DEBUG
            toolbar.internal.updateActionsImmediately()
        }
    }
}


private class ExportAction(private val toolbar: IntelliJToolbar)
    : DumbAwareAction("Export", "Export the session", toolbar.loadIcon("/ic_save.png")) {

    override fun actionPerformed(e: AnActionEvent?) {
        toolbar.listener?.onExportSelected()
    }

}

private class ClearAction(private val toolbar: IntelliJToolbar) : DumbAwareAction("Clear", "Clear current session",
        toolbar.loadIcon("/ic_delete.png")) {

    override fun actionPerformed(e: AnActionEvent?) {
        toolbar.listener?.onClearSelected()
    }

}

private class ConfigureBreakpointsAction(private val toolbar: IntelliJToolbar) :
        DumbAwareAction("Configure debugger", "Configure debugger",
                toolbar.loadIcon("/viewBreakpoints.png")) {

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
