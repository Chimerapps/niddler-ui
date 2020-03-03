package com.chimerapps.niddler.ui.debugging.rewrite

import com.chimerapps.niddler.ui.debugging.rewrite.location.EditLocationDialog
import com.chimerapps.niddler.ui.debugging.rewrite.rule.EditRewriteRuleDialog
import com.chimerapps.niddler.ui.util.ext.swap
import com.chimerapps.niddler.ui.util.ui.addChangeListener
import com.chimerapps.niddler.ui.util.ui.setColumnFixedWidth
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocationMatch
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteSet
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.Label
import com.intellij.ui.table.JBTable
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableModel

@Suppress("DuplicatedCode")
class RewriteDetailPanel(private val parentWindow: Window,
                         private val onItemUpdated: (old: RewriteSet, new: RewriteSet) -> Unit) : JPanel(GridBagLayout()) {

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

        it.addChangeListener {
            val item = currentItem ?: return@addChangeListener
            val newName = it.text.trim()
            if (item.name != newName) {
                val copy = item.copy(name = newName)
                _currentItemInternal = copy
                onItemUpdated(item, copy)
            }
        }
    }
    private val locationTable: PackingJBTable = PackingJBTable(EditableTableModel() { value, row, col ->
        if (col == 0) {
            val item = currentItem ?: return@EditableTableModel
            val locationsCopy = item.locations.toMutableList()
            locationsCopy[row] = locationsCopy[row].copy(enabled = value == true)
            val copy = item.copy(locations = locationsCopy)
            _currentItemInternal = copy
            onItemUpdated(item, copy)
        }
    }, onRowDoubleClicked = { row, model ->
        val item = currentItem ?: return@PackingJBTable
        val edited = EditLocationDialog.show(parentWindow, item.locations[row].location)
                ?: return@PackingJBTable
        val locationsCopy = item.locations.toMutableList()
        locationsCopy[row] = locationsCopy[row].copy(location = edited)
        val copy = item.copy(locations = locationsCopy)
        (model as DefaultTableModel).setValueAt(edited.asString(), row, 1)
        _currentItemInternal = copy
        onItemUpdated(item, copy)
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
            onItemUpdated(item, copy)
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
            onItemUpdated(item, copy)
        }
    }

    private val rulesTable: PackingJBTable = PackingJBTable(EditableTableModel() { value, row, col ->
        if (col == 0) {
            val item = currentItem ?: return@EditableTableModel
            val rulesCopy = item.rules.toMutableList()
            rulesCopy[row] = rulesCopy[row].copy(active = value == true)
            val copy = item.copy(rules = rulesCopy)
            _currentItemInternal = copy
            onItemUpdated(item, copy)
        }
    }, onRowDoubleClicked = { row, model ->
        val item = currentItem ?: return@PackingJBTable
        val edited = EditRewriteRuleDialog.show(parentWindow, item.rules[row])
                ?: return@PackingJBTable
        val rulesCopy = item.rules.toMutableList()
        rulesCopy[row] = edited
        val copy = item.copy(rules = rulesCopy)

        (model as DefaultTableModel).setValueAt(ruleTypeString(edited.ruleType), row, 1)
        model.setValueAt(edited.actionAsString(), row, 2)
        _currentItemInternal = copy
        onItemUpdated(item, copy)
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
            val numSelected = it.selectedRowCount
            val hasSelection = numSelected != 0
            rulesRemoveButton.isEnabled = hasSelection
            rulesRemoveButton.isEnabled = hasSelection
            rulesUpButton.isEnabled = numSelected == 1
            rulesDownButton.isEnabled = numSelected == 1
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
            val newRule = EditRewriteRuleDialog.show(parentWindow, null) ?: return@addActionListener
            val item = currentItem ?: return@addActionListener
            val rulesCopy = item.rules.toMutableList()
            rulesCopy.add(newRule)

            (rulesTable.model as DefaultTableModel).addRow(arrayOf(newRule.active, ruleTypeString(newRule.ruleType), newRule.actionAsString()))
            val copy = item.copy(rules = rulesCopy)
            _currentItemInternal = copy
            onItemUpdated(item, copy)
        }
    }
    private val rulesRemoveButton = JButton("Remove").also {
        rulesActionsPanel.add(it)
        it.addActionListener {
            val rows = rulesTable.selectedRows.reversed()
            rows.forEach { row ->
                (rulesTable.model as DefaultTableModel).removeRow(row)
            }
            rulesTable.clearSelection()
        }
    }
    private val rulesUpButton = JButton("Up").also {
        rulesActionsPanel.add(it)
        it.addActionListener {
            val row = rulesTable.selectedRow
            if (row > 0) {
                (rulesTable.model as DefaultTableModel).moveRow(row, row, row - 1)
                rulesTable.setRowSelectionInterval(row - 1, row - 1)
                rulesTable.scrollRectToVisible(rulesTable.getCellRect(row - 1, 0, true))

                val item = currentItem ?: return@addActionListener
                val rulesCopy = item.rules.toMutableList()
                rulesCopy.swap(row, row - 1)

                val copy = item.copy(rules = rulesCopy)
                _currentItemInternal = copy
                onItemUpdated(item, copy)
            }
        }
    }
    private val rulesDownButton = JButton("Down").also {
        rulesActionsPanel.add(it)
        it.addActionListener {
            val row = rulesTable.selectedRow
            if (row < rulesTable.rowCount - 1) {
                (rulesTable.model as DefaultTableModel).moveRow(row, row, row + 1)
                rulesTable.setRowSelectionInterval(row + 1, row + 1)
                rulesTable.scrollRectToVisible(rulesTable.getCellRect(row + 1, 0, true))

                val item = currentItem ?: return@addActionListener
                val rulesCopy = item.rules.toMutableList()
                rulesCopy.swap(row, row + 1)

                val copy = item.copy(rules = rulesCopy)
                _currentItemInternal = copy
                onItemUpdated(item, copy)
            }
        }
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