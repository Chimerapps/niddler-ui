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
        if (newDevices == devices)
            return

        removeAllChildren() //TODO optimize this
        devices = newDevices

        devices.forEach {
            add(ConnectDialogDeviceNode(it))
        }
    }

}

class ConnectDialogProcessNode(val device: DeviceModel, val session: NiddlerSession) : DefaultMutableTreeNode() {

    override fun isLeaf(): Boolean = true

}

class ConnectDialogDeviceNode(val device: DeviceModel) : DefaultMutableTreeNode() {

    init {
        device.sessions.forEach {
            add(ConnectDialogProcessNode(device, it))
        }
    }

}