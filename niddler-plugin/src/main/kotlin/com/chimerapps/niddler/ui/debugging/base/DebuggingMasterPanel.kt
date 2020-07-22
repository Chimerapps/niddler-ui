package com.chimerapps.niddler.ui.debugging.base

import com.chimerapps.niddler.ui.util.localization.Tr
import com.chimerapps.niddler.ui.util.ui.CheckBox
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
import com.chimerapps.niddler.ui.util.ui.chooseOpenFile
import com.chimerapps.niddler.ui.util.ui.chooseSaveFile
import com.icapps.niddler.lib.debugger.model.configuration.BaseDebuggerConfiguration
import com.icapps.niddler.lib.debugger.model.configuration.DebuggerConfigurationFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBScrollPane
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class DebuggingMasterPanel<T : BaseDebuggerConfiguration>(private val project: Project?,
                                                          private val onConfigurationAdded: (T) -> Unit,
                                                          private val onConfigurationRemoved: (Int) -> Unit,
                                                          private val selectionListener: (T?) -> Unit,
                                                          private val factory: DebuggerConfigurationFactory<T>) : JPanel(GridBagLayout()) {

    var allEnabled: Boolean = false
        set(value) {
            field = value
            configurationList.isEnabled = value
            enableAllToggle.isSelected = value

            val selectedIndices = configurationList.selectedIndices
            val enableExportAndRemove = value && selectedIndices.isNotEmpty()
            exportButton.isEnabled = enableExportAndRemove
            removeButton.isEnabled = enableExportAndRemove

            if (value && selectedIndices.size == 1) {
                selectionListener(configurationList.getItemAt(selectedIndices[0]))
            } else {
                selectionListener(null)
            }
        }

    @Suppress("MoveLambdaOutsideParentheses")
    val enableAllToggle = CheckBox(Tr.DebuggerToggleEnable.tr(), ::allEnabled).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
            gridheight = 1
            anchor = GridBagConstraints.NORTHWEST
            weightx = 100.0
        }
        add(it, constraints)
    }

    private val configurationList = CheckBoxList<T>().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.CENTER
            weightx = 100.0
            weighty = 100.0
        }
        it.isEnabled = allEnabled
        it.selectionModel.addListSelectionListener { _ ->
            val selectedIndices = it.selectedIndices
            val hasSelection = selectedIndices.isNotEmpty()
            exportButton.isEnabled = hasSelection && allEnabled
            removeButton.isEnabled = hasSelection && allEnabled

            if (allEnabled) {
                if (selectedIndices.size == 1) {
                    selectionListener(it.getItemAt(selectedIndices[0]))
                } else {
                    selectionListener(null)
                }
            } else {
                selectionListener(null)
            }

        }
        it.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        add(JBScrollPane(it).also { scroller -> scroller.border = BorderFactory.createLineBorder(Color.GRAY) }, constraints)
    }

    private val addButton = JButton(Tr.DebuggerActionAdd.tr()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            val configuration = factory.create()
            onConfigurationAdded(configuration)
            configurationList.addItem(configuration, configuration.name, configuration.active)
            configurationList.selectedIndex = configurationList.itemsCount - 1
        }
    }

    private val removeButton: JButton = JButton(Tr.DebuggerActionRemove.tr()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.EAST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            configurationList.selectedIndices.reversed().forEach { index ->
                onConfigurationRemoved(index)
                (configurationList.model as DefaultListModel).remove(index)
            }
        }
    }

    private val importButton = JButton(Tr.DebuggerActionImport.tr()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            showImportDialog()
        }
    }

    private val exportButton = JButton(Tr.DebuggerActionExport.tr()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 3
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.EAST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            showExportDialog()
        }
    }

    init {
        val enableExportAndRemove = allEnabled && configurationList.selectedIndices.isNotEmpty()
        exportButton.isEnabled = enableExportAndRemove
        removeButton.isEnabled = enableExportAndRemove
    }

    private fun showImportDialog() {
        val file = chooseOpenFile(Tr.DebuggerActionImportTitle.tr()) ?: return
        try {
            val importedData = file.inputStream.use { factory.importer().import(it) }

            importedData.forEach { configuration ->
                onConfigurationAdded(configuration)
                configurationList.addItem(configuration, configuration.name, configuration.active)
            }
        } catch (e: Throwable) {
            NotificationUtil.error(Tr.DebuggerActionImportFailedTitle.tr(), e.message ?: Tr.DebuggerActionImportFailedDescription.tr(), project)
        }
    }

    private fun showExportDialog() {
        val items = configurationList.selectedIndices.map { configurationList.getItemAt(it) }.filterNotNull()
        if (items.isEmpty()) return

        var file = chooseSaveFile(Tr.DebuggerActionExportTitle.tr(), ".xml") ?: return
        if (file.extension.isEmpty()) {
            file = File(file.absolutePath + ".xml")
        }
        file.outputStream().use { factory.exporter().export(items, it) }
        NotificationUtil.info(Tr.DebuggerActionExportSuccessTitle.tr(),
                "<html>${Tr.DebuggerActionExportSuccessMessage.tr()} <a href=\"file://${file.absolutePath}\">${file.name}</a></html>", project)
    }

    fun configurationUpdated(index: Int, new: T) {
        configurationList.updateItem(configurationList.getItemAt(index)!!, new, new.name)
    }

    fun addConfigurations(items: List<T>, selectLast: Boolean) {
        items.forEach { ruleSet ->
            onConfigurationAdded(ruleSet)
            configurationList.addItem(ruleSet, ruleSet.name, ruleSet.active)
        }
        if (selectLast)
            configurationList.selectedIndex = configurationList.itemsCount - 1
    }

    fun isConfigurationEnabled(configuration: T): Boolean {
        for (i in 0 until configurationList.itemsCount) {
            if (configurationList.getItemAt(i)?.id == configuration.id)
                return configurationList.isItemSelected(i)
        }
        return false
    }

}