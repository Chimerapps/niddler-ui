package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.AdbDevice
import java.awt.Color
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JList

class NiddlerConnectCellRenderer : DefaultListCellRenderer() {

    private var label: JLabel = JLabel()
    private val textSelectionColor = Color.BLACK
    private val backgroundSelectionColor = Color(216, 216, 216)
    private val textNonSelectionColor = Color.BLACK
    private val secondaryTextSelectionColor = Color(150, 150, 150)
    private val backgroundNonSelectionColor = Color.WHITE

    init {
        label.isOpaque = true
    }

    override fun getListCellRendererComponent(
            list: JList<*>,
            value: Any?,
            index: Int,
            selected: Boolean,
            expanded: Boolean): Component {
        if (value !is AdbDevice)
            return label
        if (value.emulator)
            label.icon = ImageIcon(javaClass.getResource("/ic_device_emulator.png"))
        else
            label.icon = ImageIcon(javaClass.getResource("/ic_device_real.png"))
        val hexColor = String.format("#%02x%02x%02x", secondaryTextSelectionColor.red, secondaryTextSelectionColor.green, secondaryTextSelectionColor.blue)
        label.text = String.format("<html>%s <font color='%s'>%s</font>", value.name, hexColor, value.extraInfo)
        label.toolTipText = value.serialNr
        if (selected) {
            label.background = backgroundSelectionColor
            label.foreground = textSelectionColor
        } else {
            label.background = backgroundNonSelectionColor
            label.foreground = textNonSelectionColor
        }

        return label
    }
}