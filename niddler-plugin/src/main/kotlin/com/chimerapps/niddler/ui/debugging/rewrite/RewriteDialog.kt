package com.chimerapps.niddler.ui.debugging.rewrite

import com.chimerapps.niddler.ui.util.ui.CheckBox
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
import com.chimerapps.niddler.ui.util.ui.chooseOpenFile
import com.chimerapps.niddler.ui.util.ui.chooseSaveFile
import com.chimerapps.niddler.ui.util.ui.setColumnFixedWidth
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteExporter
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteImporter
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocationMatch
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteSet
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableModel

class RewriteDialog(parent: Window?, private val project: Project?) : JDialog(parent, "Rewrite settings", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, project: Project?) {
            val dialog = RewriteDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            //TODO
        }
    }

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
    private val detailPanel = RewriteDetailPanel(this, ::onItemUpdated).also {
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

    private fun onItemUpdated(item: RewriteSet) {
        //TODO
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

private class RewriteDetailPanel(private val parentWindow: Window,
                                 private val onItemUpdated: (RewriteSet) -> Unit) : JPanel(GridBagLayout()) {

    private var _currentItemInternal: RewriteSet? = null

    var currentItem: RewriteSet?
        set(value) {
            if (value == _currentItemInternal) return
            _currentItemInternal = value
            updateContents()
        }
        get() = _currentItemInternal

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
    private val locationTable: PackingJBTable = PackingJBTable(EditableTableModel() { value, row, col ->
        if (col == 0) {
            val item = currentItem ?: return@EditableTableModel
            val locationsCopy = item.locations.toMutableList()
            locationsCopy[row] = locationsCopy[row].copy(enabled = value == true)
            val copy = item.copy(locations = locationsCopy)
            _currentItemInternal = copy
            onItemUpdated(copy)
        }
    }, onRowDoubleClicked = { row, model ->
        val item = currentItem ?: return@PackingJBTable
        val edited = EditLocationDialog.show(parentWindow, item.locations[row].location) ?: return@PackingJBTable
        val locationsCopy = item.locations.toMutableList()
        locationsCopy[row] = locationsCopy[row].copy(location = edited)
        val copy = item.copy(locations = locationsCopy)
        (model as DefaultTableModel).setValueAt(edited.asString(), row, 1)
        _currentItemInternal = copy
        onItemUpdated(copy)
    }).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            weightx = 100.0
            weighty = 50.0
        }
        val model = it.model as EditableTableModel
        model.addColumn("", java.lang.Boolean::class.java)
        model.addColumn("Location", String::class.java)

        it.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        it.rowSelectionAllowed = true
        it.columnSelectionAllowed = false
        it.selectionModel.addListSelectionListener { _ ->
            val hasSelection = it.selectedRowCount != 0
            locationRemoveButton.isEnabled = hasSelection
        }

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
        it.addActionListener {
            val newLocation = EditLocationDialog.show(parentWindow, null) ?: return@addActionListener

            val item = currentItem ?: return@addActionListener
            val locationsCopy = item.locations.toMutableList()
            locationsCopy.add(RewriteLocationMatch(newLocation, enabled = true))
            val copy = item.copy(locations = locationsCopy)
            (locationTable.model as DefaultTableModel).addRow(arrayOf(true, newLocation.asString()))
            _currentItemInternal = copy
            onItemUpdated(copy)
        }
    }
    private val locationRemoveButton = JButton("Remove").also {
        locationActionsPanel.add(it)
        it.addActionListener {
            val item = currentItem ?: return@addActionListener
            val rows = locationTable.selectedRows
            if (rows.isEmpty()) return@addActionListener

            val locationsCopy = item.locations.toMutableList()
            rows.reversed().forEach { row ->
                locationsCopy.removeAt(row)
                (locationTable.model as DefaultTableModel).removeRow(row)
            }
            val copy = item.copy(locations = locationsCopy)
            _currentItemInternal = copy
            onItemUpdated(copy)
        }
    }

    private val rulesTable = PackingJBTable(EditableTableModel() { value, row, col ->
    }, onRowDoubleClicked = { _, _ ->
        //TODO
    }).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            weightx = 100.0
            weighty = 50.0
        }
        val model = it.model as EditableTableModel
        model.addColumn("", java.lang.Boolean::class.java)
        model.addColumn("Type", String::class.java)
        model.addColumn("Action", String::class.java)

        it.packColumn(1)

        it.selectionModel.addListSelectionListener { _ ->
            val hasSelection = it.selectedRowCount != 0
            rulesRemoveButton.isEnabled = hasSelection
            rulesRemoveButton.isEnabled = hasSelection
            rulesUpButton.isEnabled = hasSelection
            rulesDownButton.isEnabled = hasSelection
        }

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
        it.addActionListener {
            EditRewriteRuleDialog.show(parentWindow, null)
        }
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
        (locationTable.model as DefaultTableModel).rowCount = 0
        (rulesTable.model as DefaultTableModel).rowCount = 0
    }

    private fun set(set: RewriteSet) {
        nameField.text = set.name

        (locationTable.model as DefaultTableModel).let { model ->
            model.rowCount = 0
            set.locations.forEach { model.addRow(arrayOf(it.enabled, it.location.asString())) }
        }
        (rulesTable.model as DefaultTableModel).let { model ->
            model.rowCount = 0
            set.rules.forEach { model.addRow(arrayOf(it.active, ruleTypeString(it.ruleType), it.actionAsString())) }
        }
        rulesTable.packColumn(1)
    }

    private fun ruleTypeString(ruleType: RewriteType): String {
        return when (ruleType) {
            RewriteType.ADD_HEADER -> "Append header"
            RewriteType.MODIFY_HEADER -> "Modify header"
            RewriteType.REMOVE_HEADER -> "Remove header"
            RewriteType.HOST -> "Host"
            RewriteType.PATH -> "Path"
            RewriteType.URL -> "URL"
            RewriteType.ADD_QUERY_PARAM -> "Append query"
            RewriteType.MODIFY_QUERY_PARAM -> "Modify query"
            RewriteType.REMOVE_QUERY_PARAM -> "Remove query"
            RewriteType.RESPONSE_STATUS -> "Status"
            RewriteType.BODY -> "Body"
        }
    }
}

private class PackingJBTable(model: TableModel, onRowDoubleClicked: (Int, TableModel) -> Unit) : JBTable(model) {

    init {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(mouseEvent: MouseEvent) {
                val point = mouseEvent.point
                val row = rowAtPoint(point)
                if (mouseEvent.clickCount == 2 && row != -1) {
                    onRowDoubleClicked(row, model)
                }
            }
        });
    }

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

class EditableTableModel(private val changeListener: (data: Any, row: Int, col: Int) -> Unit) : DefaultTableModel() {

    private val columnClasses = mutableListOf<Class<*>>()

    fun addColumn(name: String, clazz: Class<*>) {
        addColumn(name)
        columnClasses.add(clazz)
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return columnClasses[columnIndex]
    }

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return columnClasses[column] == java.lang.Boolean::class.java
    }

    override fun setValueAt(aValue: Any?, row: Int, column: Int) {
        aValue?.let { changeListener(it, row, column) }
        super.setValueAt(aValue, row, column)
    }

}