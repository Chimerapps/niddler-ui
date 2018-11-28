package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.lib.debugger.model.saved.WrappingDebuggerConfiguration
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.Dialog
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.StackTraceComponent
import com.icapps.niddler.ui.form.components.TabComponent
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.impl.SwingNiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.ui.AbstractToolbar
import com.icapps.niddler.ui.form.ui.SwingToolbar
import net.harawata.appdirs.AppDirsFactory
import java.awt.Window
import java.io.File
import java.io.IOException
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JScrollPane
import javax.swing.JToolBar

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class SwingComponentsFactory : ComponentsFactory {

    private companion object {
        private const val DEBUGGER_FILE = "debuggerConfig"
    }

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

    override fun createDialog(parent: Window?, title: String, content: JComponent): Dialog {
        return SwingDialog(parent, title, content)
    }

    override fun createDebugConfigurationDialog(parent: Window?, configuration: DebuggerConfiguration)
            : NiddlerDebugConfigurationDialog {
        return SwingNiddlerDebugConfigurationDialog(parent, this, configuration)
    }

    override fun loadSavedConfiguration(): DebuggerConfiguration {
        try {
            getConfigFile(DEBUGGER_FILE).reader().use {
                return WrappingDebuggerConfiguration(it)
            }
        } catch (e: IOException) {
            return WrappingDebuggerConfiguration()
        }
    }

    override fun saveConfiguration(config: DebuggerConfiguration) {
        val wrapped = config as? WrappingDebuggerConfiguration ?: WrappingDebuggerConfiguration(config)
        wrapped.save(getConfigFile(DEBUGGER_FILE), pretty = true)
    }

    override fun createHorizontalToolbar(): AbstractToolbar {
        return SwingToolbar()
    }

    override fun createVerticalToolbar(): AbstractToolbar {
        return SwingToolbar(JToolBar.VERTICAL, border = true)
    }

    override fun createTraceComponent(): StackTraceComponent? {
        return SwingStackTraceComponent()
    }

    private fun getConfigFile(name: String): File {
        val rootDir = File(AppDirsFactory.getInstance().getUserConfigDir("Niddler", null, null))
        rootDir.mkdirs()
        return File(rootDir, name)
    }
}