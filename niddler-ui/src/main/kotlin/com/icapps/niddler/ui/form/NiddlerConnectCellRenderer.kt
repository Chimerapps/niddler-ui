package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.isBright
import com.icapps.niddler.ui.toHex
import org.apache.http.util.TextUtils
import java.awt.Color
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author Koen Van Looveren
 */
class NiddlerConnectCellRenderer : DefaultTreeCellRenderer() {

    private val secondaryTextNonSelectionColor: String
    private val secondaryTextSelectionColor: String

    init {
        isOpaque = true

        secondaryTextNonSelectionColor = if (textNonSelectionColor.isBright())
            Color(0x41, 0x3F, 0x39).toHex()
        else
            textNonSelectionColor
                    .brighter().brighter()
                    .brighter().brighter().toHex()

        secondaryTextSelectionColor = if (textSelectionColor.isBright())
            Color(0xC2, 0xC2, 0xC2).toHex()
        else
            textSelectionColor
                    .brighter().brighter()
                    .brighter().brighter().toHex()

    }

    override fun getTreeCellRendererComponent(tree: JTree?, rowObject: Any?, sel: Boolean, expanded: Boolean,
                                              leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, rowObject, sel, expanded, leaf, row, hasFocus)
        background = null
        val hexColor: String
        if (selected) {
            background = backgroundSelectionColor
            foreground = textSelectionColor
            hexColor = secondaryTextSelectionColor
        } else {
            background = null
            foreground = textNonSelectionColor
            hexColor = secondaryTextNonSelectionColor
        }
        if (rowObject is NiddlerConnectDeviceTreeNode) {
            val device = rowObject.device
            if (device == null) {
                text = "No connected devices"
                icon = null
                toolTipText = null
                return this
            }
            icon = device.icon
            if (TextUtils.isEmpty(device.name)) {
                text = device.serialNr
            } else {
                text = String.format("<html>%s <font color='%s'>%s</font>", device.name, hexColor, device.extraInfo)
                toolTipText = device.serialNr
            }
        } else if (rowObject is NiddlerConnectProcessTreeNode) {
            text = String.format("<html>%s <font color='%s'>(Port: %s)</font>",
                    rowObject.session.packageName, hexColor, rowObject.session.port)
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
