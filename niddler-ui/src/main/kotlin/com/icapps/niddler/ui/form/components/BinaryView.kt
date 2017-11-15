package com.icapps.niddler.ui.form.components

import com.icapps.niddler.ui.setFixedWidth
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.event.TableColumnModelEvent
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
        val model = BinaryTableModel(table)
        val labelRenderer = LabelCellRenderer()
        val columnModel = ColumnModel(model, table, labelRenderer)

        table.background = UIManager.getColor("EditorPane.background")
        table.model = model
        table.autoCreateColumnsFromModel = false
        table.columnModel = columnModel
        table.setDefaultRenderer(Int::class.java, labelRenderer)
        table.setDefaultRenderer(String::class.java, ItemCellRenderer())
        table.rowMargin = 0

        model.bytes = File("/Users/nicolaverbeeck/bad_request.har").readBytes()

        table.autoResizeMode = JTable.AUTO_RESIZE_OFF

        add(table, BorderLayout.CENTER)
    }

}

private const val LABEL_WIDTH = 60
private const val ENTRY_WIDTH = 30

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

    override fun getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
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

private class ItemCellRenderer : TableCellRenderer {

    private val label = JLabel()

    init {
        label.horizontalAlignment = SwingConstants.CENTER
        label.font = UIManager.getFont("EditorPane.font")
        label.setFixedWidth(ENTRY_WIDTH)
    }

    override fun getTableCellRendererComponent(table: JTable?, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        label.text = value.toString()
        return label
    }
}

private class ColumnModel(private val tableModel: BinaryTableModel,
                          private val parentView: JTable,
                          private val labelRenderer: LabelCellRenderer) : DefaultTableColumnModel() {

    private val labelCol = TableColumn()
    private var previousWidth = parentView.width
    private var colCount = -1
    private var calculatedLastWidth = -1

    init {
        labelCol.width = LABEL_WIDTH
        labelCol.maxWidth = LABEL_WIDTH
        labelCol.minWidth = LABEL_WIDTH

        parentView.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                super.componentResized(e)

                if (parentView.width != previousWidth) {
                    previousWidth = parentView.width
                    val prevCol = colCount
                    val newCount = columnCount
                    if (newCount > prevCol) {
                        fireColumnAdded(TableColumnModelEvent(this@ColumnModel, prevCol, newCount))
                    } else if (newCount < prevCol) {
                        fireColumnRemoved(TableColumnModelEvent(this@ColumnModel, newCount, prevCol))
                    }
                }
            }
        })
    }

    override fun getColumnSelectionAllowed(): Boolean {
        return false
    }

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

    override fun getTotalColumnWidth(): Int {
        return (columnCount - 1) * ENTRY_WIDTH + labelCol.width
    }

    override fun getColumnCount(): Int {
        if (previousWidth <= 0)
            return 2

        if (calculatedLastWidth == previousWidth)
            return colCount

        colCount = 1
        val availableDataWidth = previousWidth - LABEL_WIDTH
        colCount += Math.max(1, availableDataWidth / ENTRY_WIDTH)

        calculatedLastWidth = previousWidth

        val rows = tableModel.rowCount
        labelRenderer.setLargestIndex(rows * colCount - 1)

        return colCount
    }

    override fun getColumn(columnIndex: Int): TableColumn {
        if (columnIndex == 0)
            return labelCol
        return TableColumn(columnIndex, ENTRY_WIDTH)
    }
}

class BinaryTableModel(private val table: JTable) : AbstractTableModel() {

    var bytes: ByteArray = ByteArray(0)
        set(value) {
            field = value
            fireTableStructureChanged()
        }

    override fun getRowCount(): Int {
        val numDataCols = columnCount - 1
        if (numDataCols > 0) {
            var base = bytes.size / numDataCols
            if ((bytes.size % numDataCols) > 0)
                ++base
            return base
        }

        return 0
    }

    override fun getColumnCount(): Int {
        return table.columnModel.columnCount
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val numDataCols = columnCount - 1
        if (numDataCols < 0)
            return ""

        if (columnIndex == 0) {
            return rowIndex * numDataCols
        }
        val index = (rowIndex * numDataCols) + (columnIndex - 1)
        if (index < bytes.size)
            return hexArray[bytes[index].toInt()]
        return ""
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex == 0) return Int::class.java
        return String::class.java
    }

}