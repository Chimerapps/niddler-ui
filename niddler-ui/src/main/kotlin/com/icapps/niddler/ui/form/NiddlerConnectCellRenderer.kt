package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.getDeviceIcon
import com.icapps.niddler.ui.toHex
import org.apache.http.util.TextUtils
import java.awt.Color
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author Koen Van Looveren
 */
class NiddlerConnectCellRenderer : DefaultTreeCellRenderer() {

    private val secondaryTextNonSelectionColor: Color = Color(150, 150, 150)

    init {
        isOpaque = true
    }

    override fun getTreeCellRendererComponent(tree: JTree?, rowObject: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, rowObject, sel, expanded, leaf, row, hasFocus)
        background = null
        val hexColor: String
        if (selected) {
            background = backgroundSelectionColor
            foreground = textSelectionColor
            hexColor = textSelectionColor.toHex()
        } else {
            background = null
            foreground = textNonSelectionColor
            hexColor = secondaryTextNonSelectionColor.toHex()
        }
        if (rowObject is NiddlerConnectDeviceTreeNode) {
            val adbDevice = rowObject.device
            if (adbDevice == null) {
                text = "No connected devices"
                icon = null
                toolTipText = null
                return this
            }
            val iconResName = getDeviceIcon(adbDevice.emulator)
            icon = ImageIcon(javaClass.getResource(iconResName))
            if (TextUtils.isEmpty(adbDevice.name)) {
                text = adbDevice.serialNr
            } else {
                text = String.format("<html>%s <font color='%s'>%s</font>", adbDevice.name, hexColor, adbDevice.extraInfo)
                toolTipText = adbDevice.serialNr
            }
        } else if (rowObject is NiddlerConnectProcessTreeNode) {
            text = rowObject.processName
            icon = null
            toolTipText = null
        } else {
            text = "No connected devices"
            icon = null
            toolTipText = null
        }
        return this
    }
}