package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.AdbDeviceModel
import javax.swing.tree.DefaultMutableTreeNode

class NiddlerConnectedDeviceTreeNode(val device: AdbDeviceModel?) : DefaultMutableTreeNode() {
    override fun isLeaf(): Boolean {
        return false
    }
}