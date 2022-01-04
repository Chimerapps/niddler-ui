package com.chimerapps.discovery.ui.renderer

import com.chimerapps.discovery.model.connectdialog.ConnectDialogDeviceNode
import com.chimerapps.discovery.model.connectdialog.ConnectDialogProcessNode
import com.chimerapps.discovery.model.connectdialog.RootNode
import com.chimerapps.discovery.ui.LocalizationDelegate
import com.chimerapps.discovery.ui.SessionIconProvider
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes

/**
 * @author Nicola Verbeeck
 */
open class ConnectDialogTreeCellDelegate {

    open fun renderRootNode(
        node: RootNode,
        iconProvider: SessionIconProvider,
        renderer: ColoredTreeCellRenderer,
        localizationDelegate: LocalizationDelegate,
    ) {
        if (node.childCount == 0)
            renderer.append(localizationDelegate.noDeviceFound)
        else
            renderer.clear()
    }

    open fun renderConnectProcessNode(
        node: ConnectDialogProcessNode,
        iconProvider: SessionIconProvider,
        renderer: ColoredTreeCellRenderer,
        localizationDelegate: LocalizationDelegate,
    ) {
        val session = node.session
        renderer.icon = session.sessionIcon?.let(iconProvider::iconForString)
        renderer.append(session.packageName)
        renderer.append(localizationDelegate.processPort(session.port), SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }

    open fun renderConnectDialogDeviceNode(
        node: ConnectDialogDeviceNode,
        iconProvider: SessionIconProvider,
        renderer: ColoredTreeCellRenderer,
        localizationDelegate: LocalizationDelegate,
    ) {
        val device = node.device
        renderer.icon = device.icon
        if (device.name.isNullOrEmpty()) {
            renderer.append(device.serialNr)
        } else {
            renderer.append(device.name)
            renderer.append(" ${device.extraInfo}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            renderer.toolTipText = device.serialNr
        }
    }
}