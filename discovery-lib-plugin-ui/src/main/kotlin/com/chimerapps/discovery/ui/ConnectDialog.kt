package com.chimerapps.discovery.ui

import com.chimerapps.discovery.device.Device
import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.adb.ADBInterface
import com.chimerapps.discovery.device.idevice.IDeviceBootstrap
import com.chimerapps.discovery.model.connectdialog.ConnectDialogDeviceNode
import com.chimerapps.discovery.model.connectdialog.ConnectDialogModel
import com.chimerapps.discovery.model.connectdialog.ConnectDialogProcessNode
import com.chimerapps.discovery.model.connectdialog.DeviceModel
import com.chimerapps.discovery.model.connectdialog.DeviceScanner
import com.chimerapps.discovery.ui.renderer.ConnectDialogTreeCellRenderer
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Window
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.beans.PropertyChangeEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.SpringLayout
import javax.swing.SwingConstants
import javax.swing.WindowConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

data class ManualConnection(val ip: String, val port: Int)
data class DiscoveredDeviceConnection(val device: Device, val session: DiscoveredSession)

data class ConnectDialogResult(val direct: ManualConnection?, val discovered: DiscoveredDeviceConnection?)

class ConnectDialog(parent: Window?, announcementPort: Int, adbInterface: ADBInterface,
                    iDeviceBootstrap: IDeviceBootstrap, sessionIconProvider: SessionIconProvider,
                    private val localization: ConnectDialogLocalization)
    : ConnectDialogUI(parent, localization.dialogTitle, sessionIconProvider, localization) {

    companion object {
        private const val PORT_MAX = 65535

        fun show(parent: Window?, adbInterface: ADBInterface, iDeviceBootstrap: IDeviceBootstrap, announcementPort: Int,
                 sessionIconProvider: SessionIconProvider = DefaultSessionIconProvider(), localization: ConnectDialogLocalization): ConnectDialogResult? {
            val dialog = ConnectDialog(parent, announcementPort, adbInterface, iDeviceBootstrap, sessionIconProvider, localization)
            dialog.pack()
            dialog.setSize(500, 350)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.devicesTree.expandPath(TreePath(dialog.deviceModel.devicesRoot.path))

            dialog.isVisible = true
            return dialog.result
        }
    }

    private val deviceScanner = DeviceScanner(adbInterface, iDeviceBootstrap, announcementPort, listener = ::onDevicesUpdated)

    var result: ConnectDialogResult? = null
        private set

    init {
        val statuses = mutableListOf<String>()
        if (!adbInterface.isRealConnection) {
            statuses += localization.adbPathNotFound
        }
        if (!iDeviceBootstrap.isRealConnection) {
            statuses += localization.iDevicePathNotFound
        }
        setStatuses(statuses)
        deviceScanner.startScanning()
        onDeviceSelectionChanged()
    }

    override fun dispose() {
        deviceScanner.stopScanning()
        super.dispose()
    }

    override fun onCancel() {
        dispose()
    }

    override fun onConnect() {
        val ip = deviceIpField.text?.trim() ?: ""
        val port = deviceIpField.text?.trim() ?: ""
        val node = devicesTree.selectionPath?.lastPathComponent

        if (ip.isEmpty() || port.isEmpty()) {
            (node as? ConnectDialogProcessNode)?.let {
                connectToSession(it.device.device, it.session)
                return
            }
            (node as? ConnectDialogDeviceNode)?.let {
                if (it.device.sessions.size == 1)
                    connectToSession(it.device.device, it.device.sessions[0])
                return
            }
            return
        }
        val parsedPort = port.toIntOrNull()
        if (parsedPort == null || parsedPort < 0 || parsedPort > PORT_MAX) {
            JOptionPane.showMessageDialog(this, localization.invalidPort, localization.invalidPortTitle, JOptionPane.ERROR_MESSAGE)
            return
        }
        connectDirectly(ip, parsedPort)
    }

    override fun onDeviceSelectionChanged() = updateButtonState()

    override fun onDeviceIpChanged() = updateButtonState()

    override fun onPortChanged() = updateButtonState()

    private fun connectToSession(device: Device, niddlerSession: DiscoveredSession) {
        result = ConnectDialogResult(direct = null, discovered = DiscoveredDeviceConnection(device, niddlerSession))
        dispose()
    }

    private fun connectDirectly(ip: String, port: Int) {
        result = ConnectDialogResult(direct = ManualConnection(ip, port), discovered = null)
        dispose()
    }

    private fun updateButtonState() {
        val node = devicesTree.selectionPath?.lastPathComponent
        connectButton.isEnabled = (node is ConnectDialogProcessNode) || (!deviceIpField.text.isNullOrBlank() && !portField.text.isNullOrBlank())
    }

    private fun onDevicesUpdated(devices: List<DeviceModel>) {
        deviceModel.updateModel(devices)
        val devicesRoot = deviceModel.devicesRoot
        devicesTree.expandPath(TreePath(devicesRoot.path))

        for (i in 0 until devicesRoot.childCount) {
            val path = (devicesRoot.getChildAt(i) as DefaultMutableTreeNode).path
            devicesTree.expandPath(TreePath(path))
        }
    }

}

abstract class ConnectDialogUI(parent: Window?, title: String,
                               sessionIconProvider: SessionIconProvider, localization: ConnectDialogLocalization) : JDialog(parent, title, ModalityType.APPLICATION_MODAL) {

    protected val deviceModel = ConnectDialogModel()
    protected val devicesTree = Tree(deviceModel).also {
        it.registerKeyboardAction({ onConnect() }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED)
        it.showsRootHandles = true
        it.isRootVisible = false
        it.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        it.cellRenderer = ConnectDialogTreeCellRenderer(sessionIconProvider, localization)
        it.selectionModel.addTreeSelectionListener { onDeviceSelectionChanged() }

        it.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2)
                    onConnect()
                else
                    super.mouseClicked(e)
            }
        })
    }
    protected val cancelButton = JButton(localization.cancel).also {
        it.addActionListener { onCancel() }
    }
    protected val connectButton = JButton(localization.connect).also {
        it.addActionListener { onConnect() }
    }
    private val deviceIpLabel = JLabel(localization.deviceIp)
    private val portLabel = JLabel(localization.portLabel)
    protected val deviceIpField = JTextField().also {
        it.addChangeListener { onDeviceIpChanged() }
    }
    protected val portField = JTextField().also {
        it.addChangeListener { onPortChanged() }
    }

    private val buttonContainer = JPanel(FlowLayout(FlowLayout.RIGHT)).also {
        it.add(connectButton)
        it.add(cancelButton)
    }
    private val manualConnectContainer = JPanel(SpringLayout()).also {
        it.add(deviceIpLabel)
        it.add(deviceIpField)
        deviceIpLabel.labelFor = deviceIpField
        it.add(portLabel)
        it.add(portField)
        portLabel.labelFor = portField

        SpringUtilities.makeCompactGrid(it, 2, 2, 0, 0, 5, 0)
    }
    private val topContainer = JPanel(BorderLayout()).also {
        it.add(JBScrollPane(devicesTree), BorderLayout.CENTER)
        it.add(manualConnectContainer, BorderLayout.SOUTH)
    }
    private val statusContainer = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
    }
    private val rootContainer = JPanel(BorderLayout()).also {
        it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(buttonContainer, BorderLayout.CENTER)
        bottomPanel.add(statusContainer, BorderLayout.SOUTH)
        it.add(topContainer, BorderLayout.CENTER)
        it.add(bottomPanel, BorderLayout.SOUTH)
    }

    init {
        contentPane = rootContainer
        rootPane.defaultButton = connectButton

        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })
        rootContainer.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    }

    //Make final
    final override fun addWindowListener(l: WindowListener?) {
        super.addWindowListener(l)
    }

    protected abstract fun onCancel()

    protected abstract fun onConnect()

    protected abstract fun onDeviceSelectionChanged()

    protected abstract fun onDeviceIpChanged()

    protected abstract fun onPortChanged()

    protected fun setStatuses(statuses: List<String>) {
        statusContainer.removeAll()
        statuses.forEach { status ->
            statusContainer.add(JBLabel(status, AllIcons.General.BalloonWarning, SwingConstants.LEFT))
        }
    }

}

private fun JTextField.addChangeListener(changeListener: (JTextField) -> Unit) {
    val dl = object : DocumentListener {
        private var lastChange = 0
        private var lastNotifiedChange = 0

        override fun insertUpdate(e: DocumentEvent) {
            changedUpdate(e)
        }

        override fun removeUpdate(e: DocumentEvent) {
            changedUpdate(e)
        }

        override fun changedUpdate(e: DocumentEvent?) {
            lastChange++
            ApplicationManager.getApplication().invokeLater {
                if (lastNotifiedChange != lastChange) {
                    lastNotifiedChange = lastChange
                    changeListener.invoke(this@addChangeListener)
                }
            }
        }
    }
    addPropertyChangeListener("document") { e: PropertyChangeEvent ->
        (e.oldValue as Document?)?.removeDocumentListener(dl)
        (e.newValue as Document?)?.addDocumentListener(dl)
        dl.changedUpdate(null)
    }
    document?.addDocumentListener(dl)
}