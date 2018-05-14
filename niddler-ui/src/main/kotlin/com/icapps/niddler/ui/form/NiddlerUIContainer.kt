package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.TimelineMessagesTableModel
import java.awt.AWTEvent
import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
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
        addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                when (e.keyChar) {
                    'r' -> popup.listener.onShowRelatedClicked(currentRowItem())
                }
            }
        })
    }

    lateinit var popup: NiddlerTableMessagePopupMenu

    override fun getPopupLocation(event: MouseEvent): Point? {
        val r = rowAtPoint(event.point)
        clearSelection()
        setRowSelectionInterval(r, r)
        return super.getPopupLocation(event)
    }

    override fun getComponentPopupMenu(): JPopupMenu? {
        if (!::popup.isInitialized)
            return super.getComponentPopupMenu()

        val current = currentRowItem()
        if (current == null)
            popup.clearExtra()
        else if (current.isRequest)
            popup.setOtherText("Show response", current)
        else
            popup.setOtherText("Show request", current)

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

    private fun currentRowItem(): ParsedNiddlerMessage? {
        val relatedRow = selectedRow
        if (relatedRow < 0) {
            return null
        }
        val model = model
        if (model is TimelineMessagesTableModel) {
            return model.getRow(selectedRow)
        }
        return null
    }
}
