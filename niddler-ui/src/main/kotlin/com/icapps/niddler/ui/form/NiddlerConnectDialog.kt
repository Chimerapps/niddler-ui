package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.adb.ADBBootstrap
import com.icapps.niddler.lib.adb.ADBDevice
import com.icapps.niddler.lib.adb.ADBInterface
import com.icapps.niddler.lib.adb.NiddlerSession
import com.icapps.niddler.ui.addChangeListener
import com.icapps.niddler.ui.expandAllNodes
import com.icapps.niddler.ui.model.AdbDeviceModel
import com.icapps.niddler.ui.util.WideSelectionTreeUI
import com.icapps.tools.aec.EmulatorFactory
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JOptionPane
import javax.swing.JTree
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

/**
 * @author Nicola Verbeeck
 * @date 17/11/16.
 */
class NiddlerConnectDialog(parent: Window?,
                           private val adbBootstrap: ADBBootstrap,
                           private val previousIp: String?,
                           private val previousPort: Int?) : ConnectDialog(parent) {

    private lateinit var initSwingWorker: NiddlerConnectSwingWorker

    companion object {
        @JvmStatic
        fun showDialog(parent: Window?, adbBootstrap: ADBBootstrap,
                       previousIp: String?, previousPort: Int?): ConnectSelection? {
            val dialog = NiddlerConnectDialog(parent, adbBootstrap, previousIp, previousPort)
            dialog.initUI()
            dialog.pack()
            dialog.setSize(500, 350)
            if (parent != null)
                dialog.setLocationRelativeTo(parent)
            dialog.isVisible = true
            return dialog.selection
        }
    }

    private var selection: ConnectSelection? = null

    private fun initUI() {
        initSwingWorker = NiddlerConnectSwingWorker(adbBootstrap, doneCallback = ::onBootstrapFinished)

        port.addActionListener { onOK() }
        directIP.addActionListener { onOK() }
        tree.apply {
            ui = WideSelectionTreeUI()
            model = DefaultTreeModel(DefaultMutableTreeNode(null))
            showsRootHandles = true
            isLargeModel = true
            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            cellRenderer = NiddlerConnectCellRenderer()
        }
        progressBar.start()
        initSwingWorker.execute()

        if (previousIp != null)
            directIP.text = previousIp
        if (previousPort != null)
            port.text = previousPort.toString()
        tree.addTreeSelectionListener {
            if (!tree.isSelectionEmpty)
                directIP.text = ""
        }
        directIP.addChangeListener {
            if (!directIP.text.isNullOrBlank())
                tree.clearSelection()
        }

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2)
                    onOK()
                else
                    super.mouseClicked(e)
            }
        })
    }

    override fun onOK() {
        val node = tree.lastSelectedPathComponent
        if (!validateContents())
            return
        if (node is NiddlerConnectDeviceTreeNode && node.device != null) {
            selection = ConnectSelection(device = node.device.device, session = null, ip = directIP.text, port = port.text.toInt())
        } else if (node is NiddlerConnectProcessTreeNode) {
            //todo add the correct selection for processes
            selection = ConnectSelection(device = node.device.device, session = null, ip = directIP.text, port = port.text.toInt())
        }
        dispose()
    }

    override fun onCancel() {
        initSwingWorker.cancel(true)
        dispose()
    }

    private fun validateContents(): Boolean {
        try {
            val int = port.text.toInt()
            if (int <= 0 || int > 65535) {
                return showError("Please enter a valid port number")
            }
        } catch (e: NumberFormatException) {
            return showError("Please enter a valid port")
        }
        if (!tree.isSelectionEmpty)
            return true

        if (directIP.text.isNullOrBlank())
            return showError("Please select a device or enter an ip address")
        return true
    }

    private fun showError(error: String): Boolean {
        JOptionPane.showMessageDialog(this, error, "Could not connect", JOptionPane.ERROR_MESSAGE)
        return false
    }

    private fun onBootstrapFinished(adbInterface: ADBInterface) {
        adbInterface.createDeviceWatcher {
            val reload = NiddlerReloadSwingWorker(adbInterface, adbBootstrap, tree, progressBar::stop)
            val devices = reload.executeOnBackgroundThread()
            MainThreadDispatcher.dispatch {
                reload.onExecutionDone(devices)
            }
        }
    }

    data class ConnectSelection(val device: ADBDevice?,
                                val session: NiddlerSession?,
                                val ip: String?,
                                val port: Int)

    private class NiddlerReloadSwingWorker(private val adbInterface: ADBInterface,
                                           private val adbBootstrap: ADBBootstrap,
                                           val tree: JTree,
                                           val doneCallback: () -> Unit) : SwingWorker<List<AdbDeviceModel>, AdbDeviceModel>() {

        private val authToken: String = File("${System.getProperty("user.home")}/.emulator_console_auth_token").readText()

        override fun doInBackground(): List<AdbDeviceModel> {
            return executeOnBackgroundThread()
        }

        fun executeOnBackgroundThread(): List<AdbDeviceModel> {
            val adb = ADBBootstrap(emptyList())
            val niddlerConnectProcess = NiddlerProcessImplementation()
            return adbInterface.devices.map { adbDevice ->
                val serial = adbDevice.serial
                val emulated = adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.characteristics") == "emulator"
                val name = getCorrectName(adb, serial, emulated)
                val sdkVersion = adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.sdk")
                val version = adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.release")
                val extraInfo = "(Android $version, API $sdkVersion)"

                AdbDeviceModel(name ?: "", extraInfo, emulated, serial, niddlerConnectProcess.getProcesses(), adbDevice)
            }
        }

        private fun getCorrectName(adb: ADBBootstrap, serial: String, emulated: Boolean): String? {
            return if (emulated) {
                val port = serial.split("-").last().toIntOrNull() ?: return getName(adb, serial)
                if (authToken.isBlank())
                    return getName(adb, serial)
                val emulator = EmulatorFactory.create(port, authToken)
                emulator.connect()
                val output = emulator.avdControl.name()
                emulator.disconnect()
                output.replace("_", " ").trim()
            } else {
                val name = getName(adb, serial)
                val manufacturer = adb.executeADBCommand("-s", serial,
                        "shell", "getprop", "ro.product.manufacturer")
                "$manufacturer $name"
            }
        }

        private fun getName(adb: ADBBootstrap, serial: String): String? {
            return adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.product.model")
        }

        override fun done() {
            onExecutionDone(get())
            super.done()
        }

        fun onExecutionDone(devices: List<AdbDeviceModel>) {
            val previousItem = tree.lastSelectedPathComponent
            var rows = 0
            devices.forEach { device ->
                val root = tree.model.root as DefaultMutableTreeNode
                val deviceNode = NiddlerConnectDeviceTreeNode(device)
                (tree.model as DefaultTreeModel).insertNodeInto(deviceNode, root, root.childCount)
                rows++
                device.processes.forEach { process ->
                    (tree.model as DefaultTreeModel).insertNodeInto(NiddlerConnectProcessTreeNode(process, device), deviceNode, deviceNode.childCount)
                    rows++
                }
            }

            //todo there is still a selection bug (by default the first row is selected. when moving up and  down the first row keeps it selection)
            if (previousItem != null && previousItem is DefaultMutableTreeNode) {
                tree.clearSelection()
                tree.selectionPath = TreePath(previousItem.path)
            } else if (devices.isNotEmpty()) {
                tree.selectionPath = TreePath((tree.model.root as DefaultMutableTreeNode).path);
            }
            tree.expandAllNodes(0, rows)
            tree.isRootVisible = false
            doneCallback()
        }
    }

    private class NiddlerConnectSwingWorker(val adbBootstrap: ADBBootstrap,
                                            val doneCallback: (ADBInterface) -> Unit)
        : SwingWorker<ADBInterface, Void>() {

        override fun doInBackground(): ADBInterface {
            return adbBootstrap.bootStrap()
        }

        override fun done() {
            super.done()
            doneCallback(get())
        }
    }
}