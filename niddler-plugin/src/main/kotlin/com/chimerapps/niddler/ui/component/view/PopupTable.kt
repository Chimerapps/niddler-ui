package com.chimerapps.niddler.ui.component.view

import com.intellij.ui.table.JBTable
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JPopupMenu
import javax.swing.table.TableModel

open class PopupTable<Model : TableModel, RowType>(model: Model, private val rowAtIndexCb: (model: Model, index: Int) -> RowType?,
                                                   private val popupMenuForSelectionCb: (model: Model, row: RowType?) -> JPopupMenu?) : JBTable(model) {

    override fun getPopupLocation(event: MouseEvent): Point? {
        val r = rowAtPoint(event.point)
        clearSelection()
        setRowSelectionInterval(r, r)
        return super.getPopupLocation(event)
    }

    override fun getComponentPopupMenu(): JPopupMenu? {
        val current = currentRowItem()

        return popupMenuForSelection(current) ?: super.getComponentPopupMenu()
    }

    protected fun currentRowItem(): RowType? {
        val relatedRow = selectedRow
        if (relatedRow < 0) {
            return null
        }
        return getRowAtIndex(relatedRow)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun getRowAtIndex(index: Int): RowType? = rowAtIndexCb(model as Model, index)

    @Suppress("UNCHECKED_CAST")
    protected open fun popupMenuForSelection(row: RowType?): JPopupMenu? = popupMenuForSelectionCb(model as Model, row)

}