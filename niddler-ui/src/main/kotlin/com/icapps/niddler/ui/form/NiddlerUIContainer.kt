package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.model.ui.LinkedMessagesRenderer
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * @author Nicola Verbeeck
 * *
 * @date 10/11/16.
 */
internal class NiddlerUIContainer(factory: ComponentsFactory) {

}

class PopupMenuSelectingJTable : JTable() {

    override fun getPopupLocation(event: MouseEvent): Point? {
        val r = rowAtPoint(event.point);
        if (r in 0..(rowCount - 1)) {
            setRowSelectionInterval(r, r);
        }
        return super.getPopupLocation(event)
    }
}
