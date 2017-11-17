package com.icapps.niddler.ui.impl

import com.icapps.niddler.ui.form.components.NiddlerToolbar
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.SimpleToolWindowPanel
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 16/11/2017.
 */
class IntelliJToolbar(panel: SimpleToolWindowPanel) : NiddlerToolbar {

    override var listener: NiddlerToolbar.ToolbarListener? = null

    internal var mode: Int = 0

    internal val internal: ActionToolbar
    val component: JComponent
        get() = internal.component

    init {
        val group = DefaultActionGroup()

        val chronologicalIcon = ImageIcon(IntelliJToolbar::class.java.getResource("/ic_chronological.png"))
        val chronologicalIconSelected = ImageIcon(IntelliJToolbar::class.java.getResource("/ic_chronological_selected.png"))

        val linkedIcon = ImageIcon(IntelliJToolbar::class.java.getResource("/ic_link.png"))
        val linkedIconSelected = ImageIcon(IntelliJToolbar::class.java.getResource("/ic_link.png"))

        group.add(TimelineAction(this, chronologicalIcon, chronologicalIconSelected))
        group.add(LinkedAction(this, linkedIcon, linkedIconSelected))

        group.addSeparator()
        group.add(ClearAction(this))
        group.addSeparator()
        group.add(ExportAction(this))

        internal = ActionManager.getInstance().createActionToolbar("Niddler", group, false)
        internal.setTargetComponent(panel)
        internal.updateActionsImmediately()
    }
}

internal class TimelineAction(private val toolbar: IntelliJToolbar,
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

internal class LinkedAction(private val toolbar: IntelliJToolbar,
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

internal class ExportAction(private val toolbar: IntelliJToolbar) : DumbAwareAction("Export", "Export the session",
        ImageIcon(IntelliJToolbar::class.java.getResource("/ic_save.png"))) {

    override fun actionPerformed(e: AnActionEvent?) {
        toolbar.listener?.onExportSelected()
    }

}

internal class ClearAction(private val toolbar: IntelliJToolbar) : DumbAwareAction("Clear", "Clear current session",
        ImageIcon(IntelliJToolbar::class.java.getResource("/ic_delete.png"))) {

    override fun actionPerformed(e: AnActionEvent?) {
        toolbar.listener?.onClearSelected()
    }

}