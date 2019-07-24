package com.chimerapps.niddler.ui.util.ui

import com.intellij.ide.actions.ShowFilePathAction
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

object NotificationUtil {

    private const val NOTIFICATION_CHANNEL = "niddler"

    fun info(title: String, message: String, project: Project?) {
        val group = NotificationGroup("${NOTIFICATION_CHANNEL}_info", NotificationDisplayType.BALLOON, true)

        val notification = group.createNotification(title, message, NotificationType.INFORMATION, ShowFilePathAction.FILE_SELECTING_LISTENER)
        Notifications.Bus.notify(notification, project)
    }

}