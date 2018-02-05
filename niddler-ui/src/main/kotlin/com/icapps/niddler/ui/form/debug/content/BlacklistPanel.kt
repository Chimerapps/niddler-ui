package com.icapps.niddler.ui.form.debug.content

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * @author nicolaverbeeck
 */
class BlacklistPanel : JPanel() {

    private val editField = JTextField()
    private val testEditField = JTextField()

    init {
        editField.maximumSize = Dimension(editField.maximumSize.width, editField.preferredSize.height)
        testEditField.maximumSize = Dimension(testEditField.maximumSize.width, testEditField.preferredSize.height)

        val box = Box.createVerticalBox()
        layout = BorderLayout()
        box.border = EmptyBorder(10, 0, 0, 5)
        box.add(JLabel("Regular expression").apply {
            alignmentX = JComponent.LEFT_ALIGNMENT
            border = EmptyBorder(0, 5, 0, 0)
        })

        val horizontalBox = Box.createHorizontalBox()
        horizontalBox.alignmentX = JComponent.LEFT_ALIGNMENT

        horizontalBox.add(editField)

        box.add(horizontalBox)
        box.add(Box.createGlue())
        add(box)
    }

    fun init(regex: String) {
        editField.text = regex
    }

}