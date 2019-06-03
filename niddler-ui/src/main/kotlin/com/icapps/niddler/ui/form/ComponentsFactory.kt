package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.connection.StaticBlacklistHandler
import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.ui.form.components.Dialog
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.StackTraceComponent
import com.icapps.niddler.ui.form.components.TabComponent
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.NiddlerStaticBreakpoointsConfigurationDialog
import com.icapps.niddler.ui.form.ui.AbstractToolbar
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

    fun createDebugConfigurationDialog(parent: Window?, configuration: DebuggerConfiguration)
            : NiddlerDebugConfigurationDialog

    fun createStaticBlacklistConfigurationDialog(parent: Window?, blacklist: List<StaticBlacklistHandler>)
            : NiddlerStaticBreakpoointsConfigurationDialog

    fun loadSavedConfiguration(): DebuggerConfiguration

    fun saveConfiguration(config: DebuggerConfiguration)

    fun createHorizontalToolbar(): AbstractToolbar

    fun createVerticalToolbar(): AbstractToolbar

    fun createTraceComponent(): StackTraceComponent?

}

