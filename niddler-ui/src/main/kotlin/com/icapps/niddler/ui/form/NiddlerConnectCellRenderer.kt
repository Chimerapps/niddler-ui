package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.getDeviceIcon
import com.icapps.niddler.ui.model.AdbDeviceModel
import com.icapps.niddler.ui.toHex
import java.awt.Color
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JList

/**
 * @author Koen Van Looveren
 */
class NiddlerConnectCellRenderer : DefaultListCellRenderer() {

    private var label: JLabel = JLabel()
    private val textSelectionColor = Color.WHITE
    private val secondaryTextSelectionColor = Color.WHITE
    private val backgroundSelectionColor = Color(10, 79, 208)

    private val textNonSelectionColor = Color.BLACK
    private val secondaryTextNonSelectionColor = Color(150, 150, 150)
    private val backgroundNonSelectionColor = Color.WHITE

    init {
        label.isOpaque = true
    }

    override fun getListCellRendererComponent(
            list: JList<*>,
            device: Any?,
            index: Int,
            selected: Boolean,
            expanded: Boolean): Component {
        if (device !is AdbDeviceModel)
            return label
        val iconResName = getDeviceIcon(device.emulator)
        label.icon = ImageIcon(javaClass.getResource(iconResName))

        val hexColor: String
        if (selected) {
            label.background = backgroundSelectionColor
            label.foreground = textSelectionColor
            hexColor = secondaryTextSelectionColor.toHex()
        } else {
            label.background = backgroundNonSelectionColor
            label.foreground = textNonSelectionColor
            hexColor = secondaryTextNonSelectionColor.toHex()
        }
        label.text = String.format("<html>%s <font color='%s'>%s</font>", device.name, hexColor, device.extraInfo)
        label.toolTipText = device.serialNr
        return label
    }
}