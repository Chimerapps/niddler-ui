package com.icapps.niddler.ui.form

import java.awt.AWTEvent
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JPopupMenu
import javax.swing.JTable
import javax.swing.table.TableModel

/**
 * @author Nicola Verbeeck
 * *
 * @date 10/11/16.
 */
abstract class PopupMenuSelectingJTable<RowType> : JTable {

    constructor() : super()
    constructor(dm: TableModel?) : super(dm)

    init {
        enableEvents(AWTEvent.MOUSE_EVENT_MASK)
    }


    override fun getPopupLocation(event: MouseEvent): Point? {
        val r = rowAtPoint(event.point)
        clearSelection()
        setRowSelectionInterval(r, r)
        return super.getPopupLocation(event)
    }

    override fun getComponentPopupMenu(): JPopupMenu? {
        val current = currentRowItem()

        return popupMenuForSelection(current)
    }

    protected abstract fun popupMenuForSelection(row: RowType?): JPopupMenu?

    protected fun currentRowItem(): RowType? {
        val relatedRow = selectedRow
        if (relatedRow < 0) {
            return null
        }
        return getRowAtIndex(relatedRow)
    }

    protected abstract fun getRowAtIndex(index: Int): RowType?

}
