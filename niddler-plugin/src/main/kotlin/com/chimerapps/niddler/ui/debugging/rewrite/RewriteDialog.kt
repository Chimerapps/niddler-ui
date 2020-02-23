package com.chimerapps.niddler.ui.debugging.rewrite

import com.chimerapps.niddler.ui.util.ui.CheckBox
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
import com.chimerapps.niddler.ui.util.ui.chooseOpenFile
import com.chimerapps.niddler.ui.util.ui.chooseSaveFile
import com.chimerapps.niddler.ui.util.ui.setColumnFixedWidth
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteExporter
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteImporter
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteSet
import com.intellij.openapi.project.Project
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.Label
import com.intellij.ui.table.JBTable
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.Window
import java.io.File
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader

class RewriteDialog(parent: Window?, private val project: Project?) : JDialog(parent, "Rewrite settings", ModalityType.APPLICATION_MODAL) {

    private val rootContainer = JPanel(GridBagLayout())

    private val masterPanel = RewriteMasterPanel(project, ::onMasterSelectionChanged).also {
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
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 10, 10) }, constraints)
    }
    private val detailPanel = RewriteDetailPanel().also {
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
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 10, 10) }, constraints)
    }

    init {
        val screenSize = Toolkit.getDefaultToolkit().screenSize

        contentPane = rootContainer
        minimumSize = Dimension(screenSize.width / 3, screenSize.height / 3)
        pack()
        size = minimumSize
    }

    private fun onMasterSelectionChanged(selectedItem: RewriteSet?) {
        detailPanel.currentItem = selectedItem
    }
}

private class RewriteMasterPanel(private val project: Project?, private val selectionListener: (RewriteSet?) -> Unit) : JPanel(GridBagLayout()) {

    var allEnabled: Boolean = false
        set(value) {
            field = value
            rulesList.isEnabled = value

            val selectedIndices = rulesList.selectedIndices
            val enableExportAndRemove = value && selectedIndices.isNotEmpty()
            exportButton.isEnabled = enableExportAndRemove
            removeButton.isEnabled = enableExportAndRemove

            if (value && selectedIndices.size == 1) {
                selectionListener(rulesList.getItemAt(selectedIndices[0]))
            } else {
                selectionListener(null)
            }
        }

    @Suppress("MoveLambdaOutsideParentheses")
    val enableAllToggle = CheckBox("Enable rewrite", ::allEnabled).also {
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

    val rulesList = CheckBoxList<RewriteSet>().also {
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

    val addButton = JButton("Add").also {
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
    }

    val removeButton: JButton = JButton("Remove").also {
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
            rulesList.selectedIndices.reversed().forEach { index ->
                (rulesList.model as DefaultListModel).remove(index)
            }
        }
    }

    val importButton = JButton("Import").also {
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

    val exportButton = JButton("Export").also {
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
        val enableExportAndRemove = allEnabled && rulesList.selectedIndices.isNotEmpty()
        exportButton.isEnabled = enableExportAndRemove
        removeButton.isEnabled = enableExportAndRemove
    }

    private fun showImportDialog() {
        val file = chooseOpenFile("Select file") ?: return
        try {
            val importedData = file.inputStream.use { RewriteImporter().import(it) }

            importedData.forEach { ruleSet ->
                rulesList.addItem(ruleSet, ruleSet.name, ruleSet.active)
            }
        } catch (e: Throwable) {
            NotificationUtil.error("Failed to import", e.message ?: "Failed to parse file", project)
        }
    }

    private fun showExportDialog() {
        val items = rulesList.selectedIndices.map { rulesList.getItemAt(it) }.filterNotNull()
        if (items.isEmpty()) return

        var file = chooseSaveFile("Export to", ".xml") ?: return
        if (file.extension.isEmpty()) {
            file = File(file.absolutePath + ".xml")
        }
        file.outputStream().use { RewriteExporter().export(items, it) }
        NotificationUtil.info("Rule export complete", "<html>Rule export completed to <a href=\"file://${file.absolutePath}\">${file.name}</a></html>", project)
    }

}

private class RewriteDetailPanel : JPanel(GridBagLayout()) {

    var currentItem: RewriteSet? = null
        set(value) {
            if (value == field) return
            field = value
            updateContents()
        }

    private val namePanel = JPanel(GridBagLayout()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.NORTHWEST
            weightx = 100.0
        }
        add(it.also { it.border = BorderFactory.createEmptyBorder(0, 0, 10, 0) }, constraints)

        Label("Name:").also { label ->
            val childConstraints = GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 1
                gridheight = 1
                anchor = GridBagConstraints.WEST
            }
            it.add(label, childConstraints)
        }
    }
    private val nameField = JBTextField().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
        namePanel.add(it, constraints)
    }
    private val locationTable = PackingJBTable().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            weightx = 100.0
            weighty = 50.0
        }
        val model = it.model as DefaultTableModel
        model.addColumn("")
        model.addColumn("Location")

        it.setColumnFixedWidth(0, 30)

        add(JBScrollPane(it).also { scroller -> scroller.border = BorderFactory.createLineBorder(Color.GRAY) }, constraints)
    }
    private val locationActionsPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
        add(it, constraints)
    }
    private val locationAddButton = JButton("Add").also {
        locationActionsPanel.add(it)
    }
    private val locationRemoveButton = JButton("Remove").also {
        locationActionsPanel.add(it)
    }

    private val rulesTable = PackingJBTable().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            weightx = 100.0
            weighty = 50.0
        }
        val model = it.model as DefaultTableModel
        model.addColumn("")
        model.addColumn("Type")
        model.addColumn("Action")

        it.packColumn(1)

        it.setColumnFixedWidth(0, 30)

        add(JBScrollPane(it).also { scroller -> scroller.border = BorderFactory.createLineBorder(Color.GRAY) }, constraints)
    }

    private val rulesActionsPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 4
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
        add(it, constraints)
    }
    private val rulesAddButton = JButton("Add").also {
        rulesActionsPanel.add(it)
    }
    private val rulesRemoveButton = JButton("Remove").also {
        rulesActionsPanel.add(it)
    }
    private val rulesUpButton = JButton("Up").also {
        rulesActionsPanel.add(it)
    }
    private val rulesDownButton = JButton("Down").also {
        rulesActionsPanel.add(it)
    }

    private fun updateContents() {
        val item = currentItem
        if (item == null)
            clear()
        else
            set(item)
    }

    private fun clear() {
        nameField.text = ""
    }

    private fun set(set: RewriteSet) {
        nameField.text = set.name
    }
}

private class PackingJBTable : JBTable() {

    override fun createDefaultTableHeader(): JTableHeader {
        return PackingHeader()
    }

    fun packColumn(index: Int) {
        (getTableHeader() as PackingHeader).doPackColumn(index)
    }

    private inner class PackingHeader : JBTable.JBTableHeader() {
        fun doPackColumn(index: Int) {
            packColumn(index)
        }
    }
}
