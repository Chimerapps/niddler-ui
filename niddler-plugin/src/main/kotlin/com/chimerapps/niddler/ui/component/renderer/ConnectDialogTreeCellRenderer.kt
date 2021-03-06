package com.chimerapps.niddler.ui.component.renderer

import com.chimerapps.discovery.model.connectdialog.ConnectDialogDeviceNode
import com.chimerapps.discovery.model.connectdialog.ConnectDialogProcessNode
import com.chimerapps.discovery.model.connectdialog.RootNode
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTree

internal class ConnectDialogTreeCellRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        when (value) {
            is RootNode -> {
                if (value.childCount == 0)
                    append("No connected devices")
                else
                    clear()
            }
            is ConnectDialogProcessNode -> {
                val session = value.session
                icon = null
                append(session.packageName)
                append(" (port: ${session.port})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
            is ConnectDialogDeviceNode -> {
                val device = value.device
                icon = device.icon
                val deviceName = device.name
                if (deviceName.isNullOrEmpty()) {
                    append(device.serialNr)
                } else {
                    append(deviceName)
                    append(" ${device.extraInfo}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    toolTipText = device.serialNr
                }
            }
        }
    }

}