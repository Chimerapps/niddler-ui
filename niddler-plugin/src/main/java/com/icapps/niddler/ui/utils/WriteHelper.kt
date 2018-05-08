package com.icapps.niddler.ui.utils

import com.intellij.openapi.application.ApplicationManager


/**
 * @author nicolaverbeeck
 */
fun runWriteAction(writeAction: () -> Unit) {
    val application = ApplicationManager.getApplication()
    if (application.isDispatchThread) {
        application.runWriteAction {
            writeAction()
        }
    } else {
        application.invokeLater {
            application.runWriteAction {
                writeAction()
            }
        }
    }
}