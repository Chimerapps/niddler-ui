package com.icapps.niddler.ui.form.components

import com.icapps.niddler.ui.setFixedWidth
import java.awt.BorderLayout
import java.awt.Component
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableColumnModel
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
class BinaryView : JPanel(BorderLayout()) {

    private val table = JTable()

    init {
        val columnModel = ColumnModel()
        val model = BinaryTableModel(table, columnModel.labelRenderer)

        table.background = UIManager.getColor("EditorPane.background")
        table.model = model
        table.autoCreateColumnsFromModel = false
        table.columnModel = columnModel
        table.rowMargin = 0
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        model.bytes = File("/Users/nicolaverbeeck/bad_request.har").readBytes()

        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        table.cellSelectionEnabled = true

        add(table, BorderLayout.CENTER)
    }

}

private const val LABEL_WIDTH = 60
private const val ENTRY_WIDTH = 30
private const val TEXTUAL_WIDTH = 30

private class LabelCellRenderer : TableCellRenderer {

    private val label = JLabel()
    private val cell = JPanel(BorderLayout())

    init {
        cell.background = UIManager.getColor("ToolBar.background")
        cell.isOpaque = true
        cell.border = BorderFactory.createMatteBorder(0, 0, 0, 1, cell.background.darker())

        label.setFixedWidth(LABEL_WIDTH)
        label.horizontalAlignment = SwingConstants.RIGHT
        label.font = UIManager.getFont("EditorPane.font")
        cell.add(label, BorderLayout.CENTER)
    }

    override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean,
                                               row: Int, column: Int): Component {
        if (value == null) {
            label.text = ""
            return label
        }
        label.text = value.toString()

        return cell
    }

    fun setLargestIndex(index: Int) {
        val width = label.getFontMetrics(label.font).stringWidth(index.toString())
        label.setFixedWidth(width)
        label.setSize(width, label.height)
    }

}

private val hexArray = Array<String>(256) {
    String.format("%02X", it)
}

private val textArray = Array<String>(256) {
    String(byteArrayOf(it.toByte(), 0))
}

private class BinaryItemCellRenderer : TableCellRenderer {

    private val label = JLabel()

    init {
        label.apply {
            horizontalAlignment = SwingConstants.CENTER
            font = UIManager.getFont("EditorPane.font")
            setFixedWidth(ENTRY_WIDTH)
        }
    }

    override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean,
                                               hasFocus: Boolean, row: Int, column: Int): Component {
        if (value == null) {
            label.text = ""
            return label
        }
        label.text = hexArray[value as Int]
        return label
    }
}

private class TextualCellRenderer : TableCellRenderer {

    private val label = JLabel()
    private val normalBg = UIManager.getColor("EditorPane.background")
    private val selectedBg = UIManager.getColor("EditorPane.selectionBackground")
    private val normalTextColor = UIManager.getColor("EditorPane.foreground")
    private val selectedTextColor = UIManager.getColor("EditorPane.selectionForeground")

    init {
        label.apply {
            isOpaque = true
            horizontalAlignment = SwingConstants.CENTER
            font = UIManager.getFont("EditorPane.font")
            background = normalBg
            foreground = normalTextColor
            setFixedWidth(TEXTUAL_WIDTH)
        }
    }

    override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean,
                                               hasFocus: Boolean, row: Int, column: Int): Component {
        if (value == null) {
            label.text = ""
            return label
        }

        label.text = textArray[value as Int]

        if (isSelected) {
            label.apply {
                background = selectedBg
                foreground = selectedTextColor
            }
        } else {
            label.apply {
                background = normalBg
                foreground = normalTextColor
            }
        }

        return label
    }
}

private class ColumnModel : DefaultTableColumnModel() {

    private val labelCol = TableColumn()
    private val binaryCol = TableColumn()
    private val textualCol = TableColumn()
    val labelRenderer = LabelCellRenderer()

    init {
        labelCol.apply {
            width = LABEL_WIDTH
            maxWidth = LABEL_WIDTH
            minWidth = LABEL_WIDTH
            cellRenderer = labelRenderer
        }
        binaryCol.apply {
            width = ENTRY_WIDTH
            maxWidth = ENTRY_WIDTH
            minWidth = ENTRY_WIDTH
            cellRenderer = BinaryItemCellRenderer()
        }
        textualCol.apply {
            width = TEXTUAL_WIDTH
            maxWidth = TEXTUAL_WIDTH
            minWidth = TEXTUAL_WIDTH
            cellRenderer = TextualCellRenderer()
        }
    }

    override fun getColumnSelectionAllowed(): Boolean = true

    override fun setColumnSelectionAllowed(flag: Boolean) {
        //Ignore
    }

    override fun setColumnMargin(newMargin: Int) {
        //Ignore
    }

    override fun addColumn(aColumn: TableColumn?) {
        //Ignore
        throw IllegalStateException("No manual column adding allowed")
    }

    override fun getColumns(): Enumeration<TableColumn> {
        return object : Enumeration<TableColumn> {

            private var index = 0
            override fun nextElement(): TableColumn {
                return getColumn(index++)
            }

            override fun hasMoreElements(): Boolean {
                return (index < columnCount)
            }
        }
    }

    override fun getColumnMargin(): Int = 0

    override fun getColumnCount(): Int = 17

    override fun getTotalColumnWidth(): Int = labelCol.width + (8 * ENTRY_WIDTH) + (8 * TEXTUAL_WIDTH)

    override fun getColumn(columnIndex: Int): TableColumn {
        if (columnIndex == 0)
            return labelCol

        if (columnIndex <= 8) {
            binaryCol.modelIndex = columnIndex
            return binaryCol
        }

        textualCol.modelIndex = columnIndex
        return textualCol
    }
}

private class BinaryTableModel(private val table: JTable,
                               private val labelCellRenderer: LabelCellRenderer) : AbstractTableModel() {

    private var lastRowCount = -1
    var bytes: ByteArray = ByteArray(0)
        set(value) {
            field = value
            fireTableStructureChanged()
        }

    override fun getRowCount(): Int {
        val numDataCols = 8
        var base = bytes.size / numDataCols
        if ((bytes.size % numDataCols) > 0)
            ++base

        if (base != lastRowCount) {
            labelCellRenderer.setLargestIndex(base)
            lastRowCount = base
        }
        return base
    }

    override fun getColumnCount(): Int = table.columnModel.columnCount

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val numDataCols = 8

        if (columnIndex == 0) {
            return rowIndex * numDataCols
        }

        var index = (rowIndex * numDataCols)

        if (columnIndex > numDataCols)
            index += (columnIndex - numDataCols - 1)
        else
            index += (columnIndex - 1)

        if (index >= bytes.size) return ""

        return bytes[index].toInt()
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

}