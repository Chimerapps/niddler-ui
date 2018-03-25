package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.adb.ADBBootstrap
import com.icapps.niddler.lib.adb.ADBDevice
import com.icapps.niddler.lib.adb.ADBInterface
import com.icapps.niddler.ui.addChangeListener
import com.icapps.niddler.ui.model.AdbDeviceModel
import com.icapps.tools.aec.EmulatorFactory
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.SwingWorker


/**
 * @author Nicola Verbeeck
 * @date 17/11/16.
 */
class NiddlerConnectDialog(parent: Window?,
                           private val adbConnection: ADBInterface,
                           private val previousIp: String?,
                           private val previousPort: Int?) : ConnectDialog(parent) {

    private lateinit var swingWorker: NiddlerConnectSwingWorker

    companion object {
        @JvmStatic
        fun showDialog(parent: Window?, adbConnection: ADBInterface,
                       previousIp: String?, previousPort: Int?): ConnectSelection? {
            val dialog = NiddlerConnectDialog(parent, adbConnection, previousIp, previousPort)
            dialog.initUI()
            dialog.pack()
            if (parent != null)
                dialog.setLocationRelativeTo(parent)
            dialog.isVisible = true
            return dialog.selection
        }
    }

    private var selection: ConnectSelection? = null

    private fun initUI() {
        port.addActionListener { onOK() }
        directIP.addActionListener { onOK() }

        adbList.cellRenderer = NiddlerConnectCellRenderer()
        swingWorker = NiddlerConnectSwingWorker(adbConnection, adbList, doneCallback = progressBar::stop)
        progressBar.start()
        swingWorker.execute()
        if (previousIp != null)
            directIP.text = previousIp
        if (previousPort != null)
            port.text = previousPort.toString()

        adbList.addListSelectionListener {
            if (!adbList.isSelectionEmpty)
                directIP.text = ""
        }
        directIP.addChangeListener {
            if (!directIP.text.isNullOrBlank())
                adbList.clearSelection()
        }

        adbList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2)
                    onOK()
                else
                    super.mouseClicked(e)
            }
        })
    }

    override fun onOK() {
        val device = adbList.model.getElementAt(adbList.selectedIndex)
        if (!validateContents())
            return

        selection = ConnectSelection((device as? AdbDeviceModel)?.device, directIP.text, port.text.toInt())
        dispose()
    }

    override fun onCancel() {
        swingWorker.cancel(true)
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
        if (!adbList.isSelectionEmpty)
            return true

        if (directIP.text.isNullOrBlank())
            return showError("Please select a device or enter an ip address")
        return true
    }

    private fun showError(error: String): Boolean {
        JOptionPane.showMessageDialog(this, error, "Could not connect", JOptionPane.ERROR_MESSAGE)
        return false
    }

    data class ConnectSelection(val device: ADBDevice?, val ip: String?, val port: Int)

    private class NiddlerConnectSwingWorker(val adbConnection: ADBInterface, val adbList: JList<Any>,
                                            val doneCallback: () -> Unit) : SwingWorker<Boolean, Void>() {

        private var authToken: String? = null


        override fun doInBackground(): Boolean {
            authToken = File("${System.getProperty("user.home")}/.emulator_console_auth_token").readText()

            val model = DefaultListModel<Any>()
            val adb = ADBBootstrap(emptyList())

            adbList.model = model
            adbConnection.devices.forEach {
                val serial = it.serial
                val emulated = adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.characteristics") == "emulator"
                val name = getCorrectName(adb, serial, emulated)
                val sdkVersion = adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.sdk")
                val version = adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.release")
                val extraInfo = "(Android $version, API $sdkVersion)"
                val device = AdbDeviceModel(name ?: "", extraInfo, emulated, serial, it)
                model.addElement(device)
                if (adbList.selectedIndex == -1) {
                    adbList.selectedIndex = 0
                }
            }
            return true
        }

        private fun getCorrectName(adb: ADBBootstrap, serial: String, emulated: Boolean): String? {
            return if (emulated) {
                val port = serial.split("-").last().toIntOrNull()
                        ?: return getName(adb, serial)
                val authToken = authToken ?: return getName(adb, serial)
                val emulator = EmulatorFactory.create(port, authToken)
                emulator.connect()
                val output = emulator.avdControl.name()
                emulator.disconnect()
                output.replace("_", " ").trim()
            } else {
                val name = getName(adb, serial)
                val manufacturer = adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.product.manufacturer")
                "$manufacturer $name"
            }
        }

        private fun getName(adb: ADBBootstrap, serial: String): String? {
            return adb.executeADBCommand("-s", serial, "shell", "getprop", "ro.product.model")
        }

        override fun done() {
            super.done()
            doneCallback.invoke()
        }
    }
}