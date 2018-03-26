package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.hex.JHexEditor
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JScrollPane


/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerImageDataPanel(message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(false, true, message) {


    init {
        initUI()
    }

    override fun initAsPretty() {
        val label = JLabel()
        if (message.bodyData is BufferedImage)
            label.icon = ImageIcon(message.bodyData as BufferedImage)
        else
            label.icon = null
        replacePanel(JScrollPane(label))
    }

    override fun initAsRaw() {
        replacePanel(JHexEditor(message.message.getBodyAsBytes))
    }

}