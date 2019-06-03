package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.hex.JHexEditor
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