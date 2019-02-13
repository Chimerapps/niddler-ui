package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.form.PopupMenuSelectingJTable
import com.icapps.niddler.ui.util.ClipboardUtil
import java.awt.datatransfer.StringSelection
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.table.AbstractTableModel

/**
 * @author Nicola Verbeeck
 * @date 14/04/2017.
 */
class NiddlerFormEncodedPanel(savedState: Map<String, Any>?, message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(true, false, savedState, message) {

    init {
        initUI()
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStructuredView() {
        structuredView = object : PopupMenuSelectingJTable<Pair<String, String>>(TwoColumnTableModel(message.bodyData as Map<String, String>)) {
            override fun popupMenuForSelection(row: Pair<String, String>?): JPopupMenu? {
                if (row == null)
                    return null
                return JPopupMenu().also {
                    it.add(JMenuItem("Copy key").also {
                        it.addActionListener {
                            ClipboardUtil.copyToClipboard(StringSelection(row.first))
                        }
                    })
                    it.add(JMenuItem("Copy value").also {
                        it.addActionListener {
                            ClipboardUtil.copyToClipboard(StringSelection(row.second))
                        }
                    })
                }
            }

            override fun getRowAtIndex(index: Int): Pair<String, String>? {
                return (model as? TwoColumnTableModel)?.itemList?.getOrNull(index)
            }

        }
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