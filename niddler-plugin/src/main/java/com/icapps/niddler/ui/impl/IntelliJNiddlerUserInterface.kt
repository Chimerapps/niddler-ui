package com.icapps.niddler.ui.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.impl.SwingNiddlerUserInterface
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
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
class IntelliJNiddlerUserInterface(private val project: Project, componentsFactory: ComponentsFactory) : SwingNiddlerUserInterface(componentsFactory) {

    private companion object {
        private const val channel = "niddler"
    }

    override val asComponent: JComponent
        get() = toolWindowPanel.component

    private val toolWindowPanel = SimpleToolWindowPanel(false, true)

    init {
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

    override fun showWarningMessage(title: String, message: String) {
        val group = NotificationGroup(
                channel + "_warning",
                NotificationDisplayType.STICKY_BALLOON,
                true
        )

        val notification = group.createNotification(title, message, NotificationType.WARNING, null)
        Notifications.Bus.notify(notification, project)
    }
}