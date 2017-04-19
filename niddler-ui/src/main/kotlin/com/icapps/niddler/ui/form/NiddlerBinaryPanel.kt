package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.hex.JHexEditor
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 * @date 19/04/2017.
 */
class NiddlerBinaryPanel(message: ParsedNiddlerMessage) : JPanel() {

    init {
        layout = BorderLayout()

        add(JHexEditor(message.bodyData as ByteArray), BorderLayout.CENTER)
    }

}