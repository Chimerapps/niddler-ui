package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfigurationProvider
import com.icapps.niddler.ui.form.components.Dialog
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.TabComponent
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import java.awt.Window
import javax.swing.JComponent
import javax.swing.JScrollPane

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
interface ComponentsFactory {

    fun createSplitPane(): SplitPane

    fun createTabComponent(): TabComponent

    fun createScrollPane(): JScrollPane

    fun showSaveDialog(title: String, extension: String): String?

    fun createDialog(parent: Window?, title: String, content: JComponent): Dialog

    fun createDebugConfigurationDialog(parent: Window?, configuration: DebuggerConfigurationProvider)
            : NiddlerDebugConfigurationDialog
}

