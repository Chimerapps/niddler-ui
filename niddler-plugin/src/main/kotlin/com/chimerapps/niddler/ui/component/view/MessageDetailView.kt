package com.chimerapps.niddler.ui.component.view

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class MessageDetailView : JPanel(BorderLayout()) {

    var currentMessage: ParsedNiddlerMessage? = null
        set(value) {
            if (field === value)
                return
            field = value
            if (value == null) {
                setEmpty()
            } else {
                updateUi(value)
            }
        }

    init {
        setEmpty()
    }

    private fun setEmpty() {
        removeAll()
        add(JLabel("Select a request/response", SwingConstants.CENTER), BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun updateUi(message: ParsedNiddlerMessage) {

    }

}