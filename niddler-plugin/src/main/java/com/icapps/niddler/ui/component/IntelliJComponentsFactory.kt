package com.icapps.niddler.ui.component

import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.lib.debugger.model.saved.WrappingDebuggerConfiguration
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.Dialog
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.TabComponent
import com.icapps.niddler.ui.form.components.impl.SwingDialog
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.impl.SwingNiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.ui.AbstractToolbar
import com.icapps.niddler.ui.util.logger
import com.icapps.niddler.ui.utils.runWriteAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.project.isDirectoryBased
import com.intellij.ui.components.JBScrollPane
import net.harawata.appdirs.AppDirsFactory
import java.awt.Window
import java.io.File
import java.io.IOException
import java.io.Reader
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JScrollPane

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class IntelliJComponentsFactory(val project: Project?, val parent: Disposable) : ComponentsFactory {

    private companion object {
        private const val DEBUGGER_FILE = "debuggerConfig"
        private val log = logger<IntelliJComponentsFactory>()
    }

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

    override fun createDialog(parent: Window?, title: String, content: JComponent): Dialog {
        return SwingDialog(parent, title, content)
    }

    override fun createDebugConfigurationDialog(parent: Window?, configuration: DebuggerConfiguration)
            : NiddlerDebugConfigurationDialog {
        return SwingNiddlerDebugConfigurationDialog(parent, this, configuration)
    }

    override fun loadSavedConfiguration(): DebuggerConfiguration {
        val reader = getConfigFile(DEBUGGER_FILE) ?: return WrappingDebuggerConfiguration()
        reader.use {
            return WrappingDebuggerConfiguration(reader)
        }
    }

    override fun saveConfiguration(config: DebuggerConfiguration) {
        val wrapped = config as? WrappingDebuggerConfiguration ?: WrappingDebuggerConfiguration(config)

        if (project?.isDirectoryBased == true) {
            writeDirectoryBased(wrapped)
        } else {
            val rootDir = File(AppDirsFactory.getInstance().getUserConfigDir("Niddler", null, null))
            rootDir.mkdirs()
            File(rootDir, DEBUGGER_FILE).writer().use {
                wrapped.save(it, pretty = true)
            }
        }
    }

    override fun createHorizontalToolbar(): AbstractToolbar {
        return IntellijGenericToolbar(horizontal = true)
    }

    override fun createVerticalToolbar(): AbstractToolbar {
        return IntellijGenericToolbar(horizontal = false)
    }

    private fun getConfigFile(name: String): Reader? {
        log.debug("Getting config file with name: $name. Is dir based: ${project?.isDirectoryBased}")
        if (project?.isDirectoryBased == true) {
            val parent = project.workspaceFile?.parent
            val niddlerDir = parent?.findChild("niddler")
            if (niddlerDir == null) {
                runWriteAction {
                    parent!!.createChildDirectory(this, "niddler")
                }
                return null
            }
            val fileChild = niddlerDir.findChild(name)
            if (fileChild == null) {
                runWriteAction {
                    niddlerDir.createChildData(this, name)
                }
                return null
            } else {
                try {
                    return File(fileChild.canonicalPath).reader()
                } catch (e: IOException) {
                    return null
                }
            }
        }

        val rootDir = File(AppDirsFactory.getInstance().getUserConfigDir("Niddler", null, null))
        rootDir.mkdirs()
        try {
            return File(rootDir, name).reader()
        } catch (e: IOException) {
            return null
        }
    }

    private fun writeDirectoryBased(wrappingDebuggerConfiguration: WrappingDebuggerConfiguration) {
        runWriteAction {
            val parent = project!!.workspaceFile?.parent
            var niddlerDir = parent?.findChild("niddler")
            if (niddlerDir == null) {
                niddlerDir = parent!!.createChildDirectory(this, "niddler")
            }
            val fileToWrite = niddlerDir.findOrCreateChildData(this, DEBUGGER_FILE).canonicalPath

            File(fileToWrite).writer().use {
                wrappingDebuggerConfiguration.save(it, pretty = true)
            }
        }
    }
}