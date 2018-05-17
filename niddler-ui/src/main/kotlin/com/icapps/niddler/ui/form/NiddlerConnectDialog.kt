package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.adb.ADBBootstrap
import com.icapps.niddler.lib.adb.ADBDevice
import com.icapps.niddler.lib.adb.ADBInterface
import com.icapps.niddler.lib.adb.NiddlerSession
import com.icapps.niddler.ui.addChangeListener
import com.icapps.niddler.ui.expandAllNodes
import com.icapps.niddler.ui.model.AdbDeviceModel
import com.icapps.niddler.ui.util.WideSelectionTreeUI
import com.icapps.niddler.ui.util.logger
import com.icapps.tools.aec.EmulatorFactory
import java.awt.Window
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JTree
import javax.swing.KeyStroke
import javax.swing.SwingWorker
import javax.swing.Timer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

/**
 * @author Nicola Verbeeck
 * @date 17/11/16.
 */
class NiddlerConnectDialog(parent: Window?,
                           private val adbBootstrap: ADBBootstrap,
                           private val previousIp: String?,
                           private val previousPort: Int?,
                           private val withDebugger: Boolean) : ConnectDialog(parent) {

    private lateinit var initSwingWorker: NiddlerConnectSwingWorker

    companion object {
        @JvmStatic
        fun showDialog(parent: Window?, adbBootstrap: ADBBootstrap,
                       previousIp: String?, previousPort: Int?, withDebugger: Boolean): ConnectSelection? {
            val dialog = NiddlerConnectDialog(parent, adbBootstrap, previousIp, previousPort, withDebugger)
            dialog.initUI()
            dialog.pack()
            dialog.setSize(500, 350)
            if (parent != null)
                dialog.setLocationRelativeTo(parent)
            dialog.isVisible = true
            return dialog.selection
        }

        val log = logger<NiddlerConnectDialog>()
        const val REFRESH_PROCESS_DELAY = 4000
    }

    private var selection: ConnectSelection? = null
    private var refreshTimer: Timer? = null
    private lateinit var adbInterface: ADBInterface
    private var reloadWorker: SwingWorker<*, *>? = null

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
            registerKeyboardAction({ onOK() }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2)
                        onOK()
                    else
                        super.mouseClicked(e)
                }
            })
            addTreeSelectionListener { event ->
                if (!tree.isSelectionEmpty)
                    directIP.text = ""

                // Invalidate the nodes that were deselected
                val treeModel = tree.model as DefaultTreeModel
                event.paths.filter { !event.isAddedPath(it) }.forEach {
                    treeModel.nodeChanged(it.lastPathComponent as DefaultMutableTreeNode)
                }
            }
            requestFocusInWindow()
        }
        progressBar.start()
        initSwingWorker.execute()

        if (previousIp != null)
            directIP.text = previousIp
        if (previousPort != null)
            port.text = previousPort.toString()
        directIP.addChangeListener {
            if (!directIP.text.isNullOrBlank())
                tree.clearSelection()
        }
    }

    override fun onOK() {
        val node = tree.lastSelectedPathComponent
        if (!validateContents())
            return
        if (node is NiddlerConnectDeviceTreeNode && node.device != null) {
            selection = ConnectSelection(device = node.device.device, session = null, ip = directIP.text, port = port.text.toInt(), withDebugger = withDebugger)
        } else if (node is NiddlerConnectProcessTreeNode) {
            //todo add the correct selection for processes
            if (withDebugger && node.session.protocolVersion < NiddlerWindow.PROTCOL_VERSION_DEBUGGING) {
                JOptionPane.showConfirmDialog(this, "This process does not support debugging")
                return
            }
            selection = ConnectSelection(device = node.device.device, session = node.session, ip = directIP.text, port = port.text.toInt(), withDebugger = withDebugger)
        } else {
            selection = ConnectSelection(null, null, directIP.text, port.text.toInt(), withDebugger)
        }
        dispose()
    }

    override fun onCancel() {
        initSwingWorker.cancel(true)
        dispose()
    }

    override fun dispose() {
        reloadWorker?.cancel(true)
        reloadWorker = null
        refreshTimer?.stop()
        refreshTimer = null
        super.dispose()
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
        this.adbInterface = adbInterface
        scheduleRefresh()
        adbInterface.createDeviceWatcher {
            reloadWorker?.cancel(true)
            reloadWorker = null
            val reload = NiddlerReloadSwingWorker(adbInterface, adbBootstrap, tree, progressBar::stop)
            val devices = reload.executeOnBackgroundThread()
            MainThreadDispatcher.dispatch {
                refreshTimer?.stop()
                refreshTimer = null
                reload.onExecutionDone(devices)
                scheduleRefresh()
            }
        }
    }

    private fun scheduleRefresh() {
        refreshTimer?.stop()
        refreshTimer = null
        refreshTimer = Timer(REFRESH_PROCESS_DELAY) {
            log.debug("Refreshing")
            try {
                reloadWorker?.cancel(true)
            } catch (e: Throwable) {
            }
            reloadWorker = NiddlerReloadSwingWorker(adbInterface, adbBootstrap, tree, progressBar::stop)
            reloadWorker?.execute()
        }.apply {
            isRepeats = true
            isCoalesce = false
            start()
        }
    }

    data class ConnectSelection(val device: ADBDevice?,
                                val session: NiddlerSession?,
                                val ip: String?,
                                val port: Int,
                                val withDebugger: Boolean)

    private class NiddlerReloadSwingWorker(private val adbInterface: ADBInterface,
                                           private val adbBootstrap: ADBBootstrap,
                                           val tree: JTree,
                                           val doneCallback: () -> Unit) : SwingWorker<List<AdbDeviceModel>, AdbDeviceModel>() {

        private val authToken: String = File("${System.getProperty("user.home")}/.emulator_console_auth_token").readText()

        override fun doInBackground(): List<AdbDeviceModel> {
            return executeOnBackgroundThread()
        }

        fun executeOnBackgroundThread(): List<AdbDeviceModel> {
            val niddlerConnectProcess = NiddlerProcessImplementation()
            val devices = adbInterface.devices
            return devices.map { adbDevice ->
                val serial = adbDevice.serial
                val emulated = adbBootstrap.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.characteristics") == "emulator"
                val name = getCorrectName(adbBootstrap, serial, emulated)
                val sdkVersion = adbBootstrap.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.sdk")
                val version = adbBootstrap.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.release")
                val extraInfo = "(Android $version, API $sdkVersion)"

                val processes = niddlerConnectProcess.getProcesses(adbDevice)
                AdbDeviceModel(name ?: "", extraInfo, emulated, serial, processes, adbDevice)
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
            val root = tree.model.root as DefaultMutableTreeNode
            root.removeAllChildren()
            (tree.model as DefaultTreeModel).reload()
            devices.forEach { device ->
                val deviceNode = NiddlerConnectDeviceTreeNode(device)
                (tree.model as DefaultTreeModel).insertNodeInto(deviceNode, root, root.childCount)
                rows++
                device.sessions.forEach { session ->
                    (tree.model as DefaultTreeModel).insertNodeInto(NiddlerConnectProcessTreeNode(session, device), deviceNode, deviceNode.childCount)
                    rows++
                }
            }

            (tree.model as DefaultTreeModel).reload()
            //TODO there is still a selection bug (by default the first row is selected. when moving up and  down the first row keeps it selection)
            if (previousItem != null && previousItem is DefaultMutableTreeNode) {
                tree.clearSelection()
                if (previousItem is NiddlerConnectProcessTreeNode) {
                    (tree.model.root as DefaultMutableTreeNode).forEach { itRoot: NiddlerConnectDeviceTreeNode ->
                        itRoot.forEach<NiddlerConnectProcessTreeNode> {
                            if (it.session == previousItem.session) {
                                tree.selectionPath = TreePath(it.path)
                            }
                        }
                    }
                } else if (previousItem is NiddlerConnectDeviceTreeNode) {
                    (tree.model.root as DefaultMutableTreeNode).forEach { itRoot: NiddlerConnectDeviceTreeNode ->
                        if (itRoot.device == previousItem.device)
                            tree.selectionPath = TreePath(itRoot.path)
                    }
                }
            } else if (devices.isNotEmpty()) {
                tree.selectionPath = TreePath((tree.model.root as DefaultMutableTreeNode).path)
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

@Suppress("UNCHECKED_CAST")
private fun <T : TreeNode> MutableTreeNode.forEach(block: (T) -> Unit) {
    val count = childCount
    for (i in 0 until count)
        block(getChildAt(i) as T)
}