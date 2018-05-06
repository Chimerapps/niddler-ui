package com.icapps.niddler.ui.form.debug.view

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.util.loadIcon
import java.util.concurrent.CompletableFuture
import javax.swing.Icon
import javax.swing.table.AbstractTableModel

/**
 * @author nicolaverbeeck
 */
class MessagesModel : AbstractTableModel() {

    companion object {
        const val COL_ICON = 0
        const val COL_METHOD = 1
        const val COL_URL = 2
    }

    private val upIcon = loadIcon("/ic_up.png")
    private val downIcon = loadIcon("/ic_down.png")
    private val backingList = mutableListOf<DebugMessageEntry>()

    fun removeMessage(message: DebugMessageEntry) {
        val messageIndex = backingList.indexOf(message)
        if (messageIndex < 0)
            return

        backingList.removeAt(messageIndex)
        fireTableRowsInserted(messageIndex, messageIndex)
    }

    fun addMessage(message: DebugMessageEntry) {
        val index = backingList.size
        backingList += message
        fireTableRowsDeleted(index, index)
    }

    override fun getRowCount(): Int = backingList.size

    override fun getColumnCount(): Int = 3

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val value = backingList[rowIndex]

        return when (columnIndex) {
            COL_ICON -> if (value.isRequest) upIcon else downIcon
            COL_METHOD -> value.method
            COL_URL -> value.url
            else -> throw IllegalStateException("Unknown column")
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            COL_ICON -> Icon::class.java
            else -> String::class.java
        }
    }

    fun getMessageAt(index: Int): DebugMessageEntry {
        return backingList[index]
    }
}

data class DebugMessageEntry(val method: String,
                             val isRequest: Boolean,
                             val url: String,
                             val future: CompletableFuture<*>,
                             val response: ParsedNiddlerMessage? = null)
