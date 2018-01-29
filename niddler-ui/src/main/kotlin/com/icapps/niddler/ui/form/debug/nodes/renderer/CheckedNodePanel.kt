package com.icapps.niddler.ui.form.debug.nodes.renderer

import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author nicolaverbeeck
 */
class CheckedNodePanel : JPanel(BorderLayout()) {

    val checkbox = JCheckBox()
    val text = JLabel()

    init {
        add(checkbox, BorderLayout.WEST)
        add(text, BorderLayout.CENTER)
    }
}