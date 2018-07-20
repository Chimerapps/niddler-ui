package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.TimelineMessagesTableModel
import java.awt.AWTEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JPopupMenu

/**
 * @author Nicola Verbeeck
 * *
 * @date 10/11/16.
 */
class TimelineJTable : PopupMenuSelectingJTable<ParsedNiddlerMessage>() {

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

    override fun popupMenuForSelection(row: ParsedNiddlerMessage?): JPopupMenu? {
        if (!::popup.isInitialized)
            return null

        when {
            row == null -> popup.clearExtra()
            row.isRequest -> popup.setOtherText("Show response", row)
            else -> popup.setOtherText("Show request", row)
        }

        return popup
    }

    override fun getRowAtIndex(index: Int): ParsedNiddlerMessage? {
        val model = model
        if (model is TimelineMessagesTableModel) {
            return model.getRow(selectedRow)
        }
        return null
    }

    fun selectRowFor(parsedNiddlerMessage: ParsedNiddlerMessage) {
        (model as? TimelineMessagesTableModel)?.let {
            val index = it.findRowIndex(parsedNiddlerMessage)
            if (index >= 0) {
                clearSelection()
                setRowSelectionInterval(index, index)
                scrollToRow(index)
            }
        }
    }

    private fun scrollToRow(index: Int) {
        scrollRectToVisible(getCellRect(index, 0, true))
    }
}
