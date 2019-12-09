package com.chimerapps.niddler.ui.component.view

import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.event.MouseInputAdapter
import javax.swing.table.TableColumn

class TableResizeAdapter(private val table: JTable, private val resizeListener: (columnIndex: Int, size: Int) -> Unit) : MouseInputAdapter() {

    private var resizingColumn: TableColumn? = null
    private var mouseXOffset: Int = 0
    private var otherCursor = resizeCursor

    init {
        table.addMouseListener(this)
        table.addMouseMotionListener(this)
    }

    private fun canResize(column: TableColumn?): Boolean {
        return (column != null
                && column.resizable)
    }

    private fun getResizingColumn(p: Point, column: Int = table.columnAtPoint(p)): TableColumn? {
        if (column == -1) {
            return null
        }
        val row = table.rowAtPoint(p)
        if (row == -1)
            return null
        val r = table.getCellRect(row, column, true)
        r.grow(-3, 0)
        if (r.contains(p))
            return null

        val midPoint = r.x + r.width / 2
        val columnIndex = if (p.x < midPoint) column - 1 else column

        return if (columnIndex == -1) null else table.columnModel.getColumn(columnIndex)
    }

    override fun mousePressed(e: MouseEvent) {
        resizingColumn = null
        val p = e.point

        // First find which header cell was hit
        val index = table.columnAtPoint(p)
        if (index == -1)
            return

        // The last 3 pixels + 3 pixels of next column are for resizing
        val resizingColumn = getResizingColumn(p, index)
        if (!canResize(resizingColumn))
            return

        this.resizingColumn = resizingColumn
        mouseXOffset = p.x - resizingColumn!!.width
    }

    private fun swapCursor() {
        val tmp = table.cursor
        table.cursor = otherCursor
        otherCursor = tmp
    }

    override fun mouseMoved(e: MouseEvent) {
        if (canResize(getResizingColumn(e.point)) != (table.cursor === resizeCursor)) {
            swapCursor()
        }
    }

    override fun mouseDragged(e: MouseEvent) {
        val mouseX = e.x

        val resizingColumn = resizingColumn ?: return

        val newWidth = mouseX - mouseXOffset
        resizingColumn.preferredWidth = newWidth
    }

    override fun mouseReleased(e: MouseEvent?) {
        resizingColumn?.let { resizeListener(it.modelIndex, it.preferredWidth) }
        resizingColumn = null
    }

    companion object {
        val resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)
    }
}