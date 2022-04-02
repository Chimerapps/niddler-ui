package com.chimerapps.discovery.model.connectdialog

import com.chimerapps.discovery.device.DiscoveredSession
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode

class ConnectDialogModel : DefaultTreeModel(DefaultMutableTreeNode()) {

    val devicesRoot = RootNode()

    init {
        root = devicesRoot
    }

    fun updateModel(devices: List<DeviceModel>) {
        devicesRoot.update(devices, this)
    }

}

class RootNode : DefaultMutableTreeNode() {

    private var devices: List<DeviceModel> = emptyList()

    val isEmpty: Boolean
        get() = devices.isEmpty()

    fun update(newDevices: List<DeviceModel>, model: DefaultTreeModel) {
        if (devices != newDevices) {
            devices = newDevices

            for (i in childCount - 1 downTo 0) {
                val node = getChildAt(i) as MutableTreeNode
                model.removeNodeFromParent(node)
            }
            devices.forEach {
                model.insertNodeInto(ConnectDialogDeviceNode(it), this, childCount)
            }
        } else {
            devices = newDevices
            devices.forEachIndexed { index, device ->
                val deviceNode = getChildAt(index) as ConnectDialogDeviceNode
                deviceNode.update(device, model)
            }
        }
    }

}

class ConnectDialogProcessNode(val device: DeviceModel, val session: DiscoveredSession) : DefaultMutableTreeNode() {

    override fun isLeaf(): Boolean = true

}

class ConnectDialogDeviceNode(var device: DeviceModel) : DefaultMutableTreeNode() {

    init {
        buildChildNodes(model = null)
    }

    fun update(device: DeviceModel, model: DefaultTreeModel) {
        //Same sessions -> ignore
        if (this.device.sessions == device.sessions) {
            this.device = device
            return
        }

        this.device = device

        for (i in childCount - 1 downTo 0) {
            val node = getChildAt(i) as MutableTreeNode
            model.removeNodeFromParent(node)
        }

        buildChildNodes(model)
    }

    private fun buildChildNodes(model: DefaultTreeModel?) {
        device.sessions.forEach {
            if (model != null) {
                model.insertNodeInto(ConnectDialogProcessNode(device, it), this, childCount)
            } else {
                add(ConnectDialogProcessNode(device, it))
            }
        }
    }

}