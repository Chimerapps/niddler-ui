package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.TimelineMessagesTableModel
import java.awt.AWTEvent
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JPopupMenu
import javax.swing.JTable

/**
 * @author Nicola Verbeeck
 * *
 * @date 10/11/16.
 */
class PopupMenuSelectingJTable : JTable() {

    init {
        enableEvents(AWTEvent.MOUSE_EVENT_MASK)
    }

    lateinit var popup: NiddlerTableMessagePopupMenu

    override fun getPopupLocation(event: MouseEvent): Point? {
        val r = rowAtPoint(event.point)
        clearSelection()
        setRowSelectionInterval(r, r)
        return super.getPopupLocation(event)
    }

    override fun getComponentPopupMenu(): JPopupMenu {
        val relatedRow = selectedRow
        if (relatedRow >= 0) {
            val model = model
            if (model is TimelineMessagesTableModel) {
                val row = model.getRow(selectedRow)
                if (row.isRequest) {
                    popup.setOtherText("Show response", row)
                } else {
                    popup.setOtherText("Show request", row)
                }
            } else {
                popup.clearExtra()
            }
        } else {
            popup.clearExtra()
        }
        return popup
    }

    fun selectRowFor(parsedNiddlerMessage: ParsedNiddlerMessage) {
        (model as? TimelineMessagesTableModel)?.let {
            val index = it.findRowIndex(parsedNiddlerMessage)
            if (index >= 0) {
                clearSelection()
                setRowSelectionInterval(index, index)
            }
        }
    }
}
