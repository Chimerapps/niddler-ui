package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

/**
 * @author Nicola Verbeeck
 * @date 14/04/2017.
 */
class NiddlerFormEncodedPanel(message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(true, false, message) {

    init {
        initUI()
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStructuredView() {
        structuredView = JTable(TwoColumnTableModel(message.bodyData as Map<String, String>))
    }

}

private class TwoColumnTableModel(items: Map<String, String>) : AbstractTableModel() {

    val itemList: List<Pair<String, String>> = items.entries.map { entry: Map.Entry<String, String> ->
        Pair(entry.key, entry.value)
    }

    override fun getRowCount(): Int {
        return itemList.size
    }

    override fun getColumnCount(): Int {
        return 2
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            0 -> "Key"
            else -> "Value"
        }
    }

    override fun getValueAt(row: Int, column: Int): Any {
        val item = itemList[row]
        return when (column) {
            0 -> item.first
            else -> item.second
        }
    }

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }
}