package com.chimerapps.niddler.ui.util.ui

import com.intellij.openapi.application.ApplicationManager
import java.beans.PropertyChangeEvent
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

/**
 * @param columnIndex   The index of the column to adjust
 * @param width The preferred with to set. Passing a negative value has no effect
 */
fun JTable.setColumnPreferredWidth(columnIndex: Int, width: Int) {
    if (width < 0) return
    val column = columnModel.getColumn(columnIndex) ?: return
    column.preferredWidth = width
}

fun JTable.setColumnFixedWidth(columnIndex: Int, width: Int) {
    if (width < 0) return
    val column = columnModel.getColumn(columnIndex) ?: return
    column.preferredWidth = width
    column.maxWidth = width
    column.minWidth = width
}

fun dispatchMain(toExecute: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(toExecute)
}

fun ensureMain(toExecute: () -> Unit) {
    if (ApplicationManager.getApplication().isDispatchThread)
        toExecute()
    else
        dispatchMain(toExecute)
}

fun <T: JTextField> T.addChangeListener(changeListener: (T) -> Unit) {
    val dl = object : DocumentListener {
        private var lastChange = 0
        private var lastNotifiedChange = 0

        override fun insertUpdate(e: DocumentEvent) {
            changedUpdate(e)
        }

        override fun removeUpdate(e: DocumentEvent) {
            changedUpdate(e)
        }

        override fun changedUpdate(e: DocumentEvent?) {
            lastChange++
            dispatchMain {
                if (lastNotifiedChange != lastChange) {
                    lastNotifiedChange = lastChange
                    changeListener.invoke(this@addChangeListener)
                }
            }
        }
    }
    addPropertyChangeListener("document") { e: PropertyChangeEvent ->
        (e.oldValue as Document?)?.removeDocumentListener(dl)
        (e.newValue as Document?)?.addDocumentListener(dl)
        dl.changedUpdate(null)
    }
    document?.addDocumentListener(dl)
}

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