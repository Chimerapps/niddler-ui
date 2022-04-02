package com.chimerapps.discovery.ui.renderer

import com.chimerapps.discovery.model.connectdialog.ConnectDialogDeviceNode
import com.chimerapps.discovery.model.connectdialog.ConnectDialogProcessNode
import com.chimerapps.discovery.model.connectdialog.RootNode
import com.chimerapps.discovery.ui.LocalizationDelegate
import com.chimerapps.discovery.ui.SessionIconProvider
import com.intellij.ui.ColoredTreeCellRenderer
import javax.swing.JTree

internal class ConnectDialogTreeCellRenderer(
    private val iconProvider: SessionIconProvider,
    private val localizationDelegate: LocalizationDelegate,
    private val delegate: ConnectDialogTreeCellDelegate,
) : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ) {
        when (value) {
            is RootNode -> delegate.renderRootNode(value, iconProvider, this, localizationDelegate)
            is ConnectDialogProcessNode -> delegate.renderConnectProcessNode(value, iconProvider, this, localizationDelegate)
            is ConnectDialogDeviceNode -> delegate.renderConnectDialogDeviceNode(value, iconProvider, this, localizationDelegate)
        }
    }

}