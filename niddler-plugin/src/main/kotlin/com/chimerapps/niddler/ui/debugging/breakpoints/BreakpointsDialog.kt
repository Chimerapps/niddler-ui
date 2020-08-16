package com.chimerapps.niddler.ui.debugging.breakpoints

import com.chimerapps.niddler.ui.debugging.base.DebuggingMasterPanel
import com.chimerapps.niddler.ui.model.ProjectConfig
import com.chimerapps.niddler.ui.util.localization.Tr
import com.icapps.niddler.lib.debugger.model.breakpoint.Breakpoint
import com.icapps.niddler.lib.debugger.model.breakpoint.BreakpointDebuggerConfigurationFactory
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.intellij.openapi.project.Project
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.Window
import java.util.UUID
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

class BreakpointsDialog(parent: Window?, project: Project) : JDialog(parent, Tr.BreakpointsConfigureTitle.tr(), ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, project: Project): BreakpointConfig? {
            val dialog = BreakpointsDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.configuration
        }

        fun showAdd(parent: Window?, project: Project, message: NiddlerMessageInfo): BreakpointConfig? {
            val dialog = BreakpointsDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            //TODO dialog.addNewRuleFor(message)

            dialog.isVisible = true
            return dialog.configuration
        }
    }

    var configuration: BreakpointConfig? = null
        private set

    private val rootContainer = JPanel(GridBagLayout())
    private val breakpoints = mutableListOf<Breakpoint>()

    private val masterPanel = DebuggingMasterPanel(project, ::onBreakpointAdded, ::onBreakpointRemoved,
            ::onMasterSelectionChanged, BreakpointDebuggerConfigurationFactory).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.NORTHWEST
            weightx = 35.0
            weighty = 100.0
        }
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 5, 10) }, constraints)
    }

    private val detailPanel = BreakpointsDetailPanel(this, ::onItemUpdated).also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.NORTHEAST
            weightx = 65.0
            weighty = 100.0
        }
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 5, 10) }, constraints)
    }
    private val buttonPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            gridheight = 1
            anchor = GridBagConstraints.SOUTHEAST
            weightx = 100.0
        }
        it.layout = BoxLayout(it, BoxLayout.X_AXIS)
        it.border = BorderFactory.createEmptyBorder(0, 0, 10, 10)
        rootContainer.add(it, constraints)
        it.add(Box.createGlue())
    }
    private val cancelButton = JButton(Tr.BreakpointsConfigureCancel.tr()).also {
        buttonPanel.add(it)
        it.addActionListener {
            dispose()
        }
    }
    private val okButton = JButton(Tr.BreakpointsConfigureOk.tr()).also {
        buttonPanel.add(it)
        it.addActionListener {
            for (i in 0 until breakpoints.size) {
                breakpoints[i] = breakpoints[i].copy(active = masterPanel.isConfigurationEnabled(breakpoints[i]))
            }
            configuration = BreakpointConfig(masterPanel.allEnabled, breakpoints)
            dispose()
        }
    }

    init {
        contentPane = rootContainer
        rootPane.defaultButton = okButton

        val screenSize = Toolkit.getDefaultToolkit().screenSize

        val minSize = Dimension(screenSize.width / 3, screenSize.height / 3)
        minimumSize = minSize
        size = minSize

        ProjectConfig.load<BreakpointConfig>(project, ProjectConfig.CONFIG_BREAKPOINTS)?.let {
            val config = it.copy(breakpoints = it.breakpoints.createIds())
            masterPanel.allEnabled = config.allEnabled
            masterPanel.addConfigurations(config.breakpoints, selectLast = false)
        }
    }

    private fun onBreakpointRemoved(index: Int) {
        breakpoints.removeAt(index)
    }

    private fun onBreakpointAdded(breakpoint: Breakpoint) {
        breakpoints.add(breakpoint)
    }

    private fun onMasterSelectionChanged(selectedItem: Breakpoint?) {
        detailPanel.currentItem = selectedItem
    }

    private fun onItemUpdated(old: Breakpoint, new: Breakpoint) {
        val oldIndex = breakpoints.indexOf(old)
        if (oldIndex < 0) return
        breakpoints[oldIndex] = new
        masterPanel.configurationUpdated(oldIndex, new)
    }

}

data class BreakpointConfig(val allEnabled: Boolean, val breakpoints: List<Breakpoint>)

private fun List<Breakpoint>.createIds(): List<Breakpoint> {
    return map { it.copy(id = UUID.randomUUID().toString()) }
}
