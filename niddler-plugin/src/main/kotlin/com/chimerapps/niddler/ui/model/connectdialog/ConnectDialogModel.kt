package com.chimerapps.niddler.ui.model.connectdialog

import com.icapps.niddler.lib.device.NiddlerSession
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class ConnectDialogModel : DefaultTreeModel(DefaultMutableTreeNode()) {

    val devicesRoot = RootNode()

    init {
        root = devicesRoot
    }

    fun updateModel(devices: List<DeviceModel>) {
        devicesRoot.update(devices)
    }

}

class RootNode : DefaultMutableTreeNode() {

    private var devices: List<DeviceModel> = emptyList()

    fun update(newDevices: List<DeviceModel>) {
        if (devices != newDevices) {
            devices = newDevices
            removeAllChildren() //TODO optimize this?
            devices.forEach {
                add(ConnectDialogDeviceNode(it))
            }
        } else {
            devices = newDevices
            devices.forEachIndexed { index, device ->
                val deviceNode = getChildAt(index) as ConnectDialogDeviceNode
                deviceNode.update(device)
            }
        }
    }

}

class ConnectDialogProcessNode(val device: DeviceModel, val session: NiddlerSession) : DefaultMutableTreeNode() {

    override fun isLeaf(): Boolean = true

}

class ConnectDialogDeviceNode(var device: DeviceModel) : DefaultMutableTreeNode() {

    init {
        buildChildNodes()
    }

    fun update(device: DeviceModel) {
        //Same sessions -> ignore
        if (this.device.sessions == device.sessions) {
            this.device = device
            return
        }

        this.device = device

        removeAllChildren()
        buildChildNodes()
    }

    private fun buildChildNodes() {
        device.sessions.forEach {
            add(ConnectDialogProcessNode(device, it))
        }
    }

}