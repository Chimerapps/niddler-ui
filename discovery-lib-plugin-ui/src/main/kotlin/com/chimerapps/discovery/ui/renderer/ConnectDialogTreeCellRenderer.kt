package com.chimerapps.discovery.ui.renderer

import com.chimerapps.discovery.model.connectdialog.ConnectDialogDeviceNode
import com.chimerapps.discovery.model.connectdialog.ConnectDialogProcessNode
import com.chimerapps.discovery.model.connectdialog.RootNode
import com.chimerapps.discovery.ui.DefaultSessionIconProvider
import com.chimerapps.discovery.ui.SessionIconProvider
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTree

internal class ConnectDialogTreeCellRenderer(private val iconProvider: SessionIconProvider) : ColoredTreeCellRenderer() {

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
                icon = session.sessionIcon?.let(iconProvider::iconForString)
                append(session.packageName)
                append(" (port: ${session.port})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
            is ConnectDialogDeviceNode -> {
                val device = value.device
                icon = device.icon
                if (device.name.isNullOrEmpty()) {
                    append(device.serialNr)
                } else {
                    append(device.name)
                    append(" ${device.extraInfo}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    toolTipText = device.serialNr
                }
            }
        }
    }

}