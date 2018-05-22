package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.DeviceModel
import javax.swing.tree.DefaultMutableTreeNode

class NiddlerConnectDeviceTreeNode(val device: DeviceModel?) : DefaultMutableTreeNode() {
    override fun isLeaf(): Boolean {
        return false
    }
}