package com.icapps.niddler.ui.form.debug.content

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Box
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author nicolaverbeeck
 */
class BlacklistPanel : JPanel() {

    private val editField = JTextField()

    init {
        editField.maximumSize = Dimension(editField.maximumSize.width, editField.preferredSize.height)

        val box = Box.createVerticalBox()
        layout = BorderLayout()
        box.add(JLabel("Regular expression"))
        box.add(editField)
        box.add(Box.createGlue())
        add(box)
    }

    fun init(regex: String) {
        editField.text = regex
    }

}