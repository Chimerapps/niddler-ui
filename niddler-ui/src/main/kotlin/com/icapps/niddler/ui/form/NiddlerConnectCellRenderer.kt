package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.getDeviceIcon
import com.icapps.niddler.ui.model.AdbDevice
import com.icapps.niddler.ui.toHex
import org.apache.http.util.TextUtils
import java.awt.Color
import java.awt.Component
import javax.swing.*

/**
 * @author Koen Van Looveren
 */
class NiddlerConnectCellRenderer : DefaultListCellRenderer() {

    private var label: JLabel = JLabel()
    private val textSelectionColor = Color.WHITE
    private val backgroundSelectionColor: Color = UIManager.getDefaults().getColor("List.selectionBackground")

    private val textNonSelectionColor: Color = UIManager.getDefaults().getColor("Label.foreground")
    private val secondaryTextNonSelectionColor: Color = Color(150, 150, 150)

    init {
        label.isOpaque = true
    }

    override fun getListCellRendererComponent(
            list: JList<*>,
            device: Any?,
            index: Int,
            selected: Boolean,
            expanded: Boolean): Component {
        if (device !is AdbDevice)
            return label
        val iconResName = getDeviceIcon(device.emulator)
        label.icon = ImageIcon(javaClass.getResource(iconResName))

        val hexColor: String
        if (selected) {
            label.background = backgroundSelectionColor
            label.foreground = textSelectionColor
            hexColor = textSelectionColor.toHex()
        } else {
            label.background = null
            label.foreground = textNonSelectionColor
            hexColor = secondaryTextNonSelectionColor.toHex()
        }
        if (TextUtils.isEmpty(device.name)) {
            label.text = device.serialNr
        } else {
            label.text = String.format("<html>%s <font color='%s'>%s</font>", device.name, hexColor, device.extraInfo)
            label.toolTipText = device.serialNr
        }
        return label
    }
}