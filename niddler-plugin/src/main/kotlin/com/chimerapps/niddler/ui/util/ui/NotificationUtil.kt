package com.chimerapps.niddler.ui.util.ui

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

object NotificationUtil {

    private const val NOTIFICATION_CHANNEL = "niddler"

    fun info(title: String, message: String, project: Project?) {
        val group = NotificationGroup("${NOTIFICATION_CHANNEL}_info", NotificationDisplayType.BALLOON, true)

        val notification = group.createNotification(title, message, NotificationType.INFORMATION, RevealFileAction.FILE_SELECTING_LISTENER)
        Notifications.Bus.notify(notification, project)
    }

    fun error(title: String, message: String, project: Project?) {
        val group = NotificationGroup("${NOTIFICATION_CHANNEL}_error", NotificationDisplayType.BALLOON, true)

        val notification = group.createNotification(title, message, NotificationType.ERROR, null)
        Notifications.Bus.notify(notification, project)
    }

    fun debug(title: String, message: String, project: Project?) {
        val group = NotificationGroup("${NOTIFICATION_CHANNEL}_info", NotificationDisplayType.TOOL_WINDOW, true)

        val notification = group.createNotification(title, message, NotificationType.INFORMATION, RevealFileAction.FILE_SELECTING_LISTENER)
        Notifications.Bus.notify(notification, project)
    }

}