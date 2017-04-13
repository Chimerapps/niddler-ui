package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.ScrollPaneConstants

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerPlainDataPanel(message: ParsedNiddlerMessage) : JPanel() {

    init {
        layout = BorderLayout()
        val monospaceFont = Font("Monospaced", Font.PLAIN, 10)
        val textArea = JTextArea()
        textArea.text = message.message.getBodyAsString(message.bodyFormat.encoding)
        textArea.isEditable = false
        textArea.font = monospaceFont

        val scroll = JScrollPane(textArea)
        scroll.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        scroll.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        add(scroll, BorderLayout.CENTER)
    }

}