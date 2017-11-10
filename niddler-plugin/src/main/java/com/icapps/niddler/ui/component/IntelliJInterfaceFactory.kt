package com.icapps.niddler.ui.component

import com.icapps.niddler.ui.form.InterfaceFactory
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.TabComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import javax.swing.JFileChooser
import javax.swing.JScrollPane

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class IntelliJInterfaceFactory(val project: Project?, val parent: Disposable) : InterfaceFactory {

    override fun createSplitPane(): SplitPane {
        return IntelliJSplitPane()
    }

    override fun createTabComponent(): TabComponent {
        return IntelliJTabComponent(project, parent)
    }

    override fun createScrollPane(): JScrollPane {
        return JBScrollPane()
    }

    override fun showSaveDialog(title: String, extension: String): String? {
        val dialog = JFileChooser()
        dialog.dialogTitle = title
        if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            return dialog.selectedFile.absolutePath
        }
        return null //TODO Make intellij version
    }
}