package com.chimerapps.niddler.ui.util.ui

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

object NotificationUtil {

    private const val NOTIFICATION_CHANNEL = "niddler"

    fun info(title: String, message: String, project: Project?) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_CHANNEL)

        group.createNotification(message, NotificationType.INFORMATION)
            .setTitle(title)
            .addAction(RevealFileAction())
            .notify(project)
    }
    fun error(title: String, message: String, project: Project?) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_CHANNEL)

        group.createNotification(message, NotificationType.ERROR)
            .setTitle(title)
            .addAction(RevealFileAction())
            .notify(project)
    }
    fun debug(title: String, message: String, project: Project?) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_CHANNEL)

        group.createNotification(message, NotificationType.INFORMATION)
            .setTitle(title)
            .addAction(RevealFileAction())
            .notify(project)
    }

}