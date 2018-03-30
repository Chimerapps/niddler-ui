package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.AdbDeviceModel
import javax.swing.tree.DefaultMutableTreeNode

class NiddlerConnectProcessTreeNode(val processName: String, val device: AdbDeviceModel) : DefaultMutableTreeNode() {
    override fun isLeaf(): Boolean {
        return true
    }
}
