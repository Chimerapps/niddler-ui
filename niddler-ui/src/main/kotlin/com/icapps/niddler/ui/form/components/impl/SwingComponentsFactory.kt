package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.TabComponent
import javax.swing.*

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class SwingComponentsFactory : ComponentsFactory {

    override fun createScrollPane(): JScrollPane {
        return JScrollPane()
    }

    override fun createSplitPane(): SplitPane {
        return SwingSplitPane()
    }

    override fun createTabComponent(): TabComponent {
        return SwingTabComponent()
    }

    override fun showSaveDialog(title: String, extension: String): String? {
        val dialog = JFileChooser()
        dialog.dialogTitle = title
        if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            return dialog.selectedFile.absolutePath
        }
        return null
    }

}