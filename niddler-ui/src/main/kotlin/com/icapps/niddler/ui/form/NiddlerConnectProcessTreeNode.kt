package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.device.NiddlerSession
import com.icapps.niddler.ui.model.DeviceModel
import javax.swing.tree.DefaultMutableTreeNode

class NiddlerConnectProcessTreeNode(val session: NiddlerSession, val device: DeviceModel) : DefaultMutableTreeNode() {
    override fun isLeaf(): Boolean {
        return true
    }
}
