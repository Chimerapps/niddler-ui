package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ui.setColumnFixedWidth
import com.intellij.ui.table.JBTable
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel

class HexViewer : JBTable() {

    fun postInit() {
        rowHeight = 32
        fillsViewportHeight = false
        showHorizontalLines = true
        showVerticalLines = true
        autoResizeMode = JTable.AUTO_RESIZE_OFF
        tableHeader = null
        selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
    }

    override fun getRowHeight(row: Int): Int {
        return 32
    }

    override fun getRowHeight(): Int {
        return 32
    }

    fun setData(data: ByteArray?) {
        val wordSize = 8
        model = HexViewerModel(data ?: ByteArray(0), wordSize)

        for (i in 1..wordSize) {
            setColumnFixedWidth(i, 32)
        }
    }

}

private class HexViewerModel(private val data: ByteArray, private val wordSize: Int) : AbstractTableModel() {

    private companion object {
        private val byteAsHex: Array<String> = Array(0xFF + 1) {
            String.format("%02X", it)
        }
    }

    private val numDataColumns = wordSize
    private val dataSize = data.size
    private val numRows = dataSize / numDataColumns + (if (dataSize % numDataColumns == 0) 0 else 1)

    override fun getRowCount(): Int = numRows

    override fun getColumnCount(): Int = numDataColumns + 1

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        if (columnIndex == 0) {
            return (rowIndex * wordSize).toString()
        }
        val dataIndex = rowIndex * numDataColumns + (columnIndex - 1)
        if (dataIndex >= data.size)
            return ""
        val byte = data[dataIndex]
        val index = byte.toInt() and 0xFF
        return byteAsHex[index]
    }

    override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

}