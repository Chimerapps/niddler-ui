package com.chimerapps.niddler.ui.component

import com.chimerapps.niddler.ui.component.renderer.ConnectDialogTreeCellRenderer
import com.chimerapps.niddler.ui.model.connectdialog.ConnectDialogModel
import com.chimerapps.niddler.ui.model.connectdialog.DeviceModel
import com.chimerapps.niddler.ui.model.connectdialog.DeviceScanner
import com.chimerapps.niddler.ui.util.ui.SpringUtilities
import com.icapps.niddler.lib.device.adb.ADBInterface
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
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.SpringLayout
import javax.swing.WindowConstants
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel


class ConnectDialog(parent: Window?, adbInterface: ADBInterface) : ConnectDialogUI(parent, "Select a device to connect to") {

    companion object {
        fun show(parent: Window?, adbInterface: ADBInterface) {
            val dialog = ConnectDialog(parent, adbInterface)
            dialog.pack()
            dialog.setSize(500, 350)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.devicesTree.expandPath(TreePath(dialog.deviceModel.devicesRoot.path))

            dialog.isVisible = true
        }
    }

    private val deviceScanner = DeviceScanner(adbInterface, ::onDevicesUpdated)

    init {
        deviceScanner.startScanning()
    }

    override fun dispose() {
        deviceScanner.stopScanning()
        super.dispose()
    }

    override fun onCancel() {
        dispose()
    }

    override fun onConnect() {
        dispose()
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

abstract class ConnectDialogUI(parent: Window?, title: String) : JDialog(parent, title, ModalityType.APPLICATION_MODAL) {

    protected val deviceModel = ConnectDialogModel()
    protected val devicesTree = Tree(deviceModel).also {
        it.registerKeyboardAction({ onConnect() }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED)
        it.showsRootHandles = true
        it.isRootVisible = false
        it.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        it.cellRenderer = ConnectDialogTreeCellRenderer()

        it.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2)
                    onConnect()
                else
                    super.mouseClicked(e)
            }
        })
    }
    protected val cancelButton = JButton("Cancel").also {
        it.addActionListener { onCancel() }
    }
    protected val connectButton = JButton("Connect").also {
        it.addActionListener { onConnect() }
    }
    private val deviceIpLabel = JLabel("Device ip:")
    private val portLabel = JLabel("Port:")
    protected val deviceIpField = JTextField()
    protected val portField = JTextField()

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
    private val rootContainer = JPanel(BorderLayout()).also {
        it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        it.add(buttonContainer, BorderLayout.SOUTH)
        it.add(topContainer, BorderLayout.CENTER)
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

}