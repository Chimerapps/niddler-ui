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

        adbList.cellRenderer = NiddlerConnectCellRenderer()

        progressBar.start()
        initSwingWorker.execute()

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

    private fun onBootstrapFinished(adbInterface: ADBInterface) {
        adbInterface.createDeviceWatcher {
            val reload = NiddlerReloadSwingWorker(adbInterface, adbBootstrap, adbList, progressBar::stop)
            val devices = reload.executeOnBackgroundThread()
            MainThreadDispatcher.dispatch {
                reload.onExecutionDone(devices)
            }
        }
    }

    data class ConnectSelection(val device: ADBDevice?, val ip: String?, val port: Int)

    private class NiddlerReloadSwingWorker(private val adbInterface: ADBInterface,
                                           private val adbBootstrap: ADBBootstrap,
                                           val adbList: JList<Any>,
                                           val doneCallback: () -> Unit) : SwingWorker<List<AdbDeviceModel>, Void>() {

        private lateinit var authToken: String

        override fun doInBackground(): List<AdbDeviceModel> {
            return executeOnBackgroundThread()
        }

        fun executeOnBackgroundThread(): List<AdbDeviceModel> {
            authToken = File("${System.getProperty("user.home")}/.emulator_console_auth_token").readText()

            return adbInterface.devices.map {
                val serial = it.serial
                val emulated = adbBootstrap.executeADBCommand("-s", serial,
                        "shell", "getprop", "ro.build.characteristics") == "emulator"

                val name = getCorrectName(adbBootstrap, serial, emulated)
                val sdkVersion = adbBootstrap.executeADBCommand("-s", serial,
                        "shell", "getprop", "ro.build.version.sdk")

                val version = adbBootstrap.executeADBCommand("-s", serial,
                        "shell", "getprop", "ro.build.version.release")

                val extraInfo = "(Android $version, API $sdkVersion)"
                AdbDeviceModel(name ?: "", extraInfo, emulated, serial, it)
            }
        }

        private fun getCorrectName(adb: ADBBootstrap, serial: String, emulated: Boolean): String? {
            return if (emulated) {
                val port = serial.split("-").last().toIntOrNull()
                        ?: return getName(adb, serial)
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
            super.done()

            onExecutionDone(get())
        }

        fun onExecutionDone(devices: List<AdbDeviceModel>) {
            val previousItem = adbList.selectedValue

            val model = DefaultListModel<Any>()
            devices.forEach(model::addElement)
            adbList.model = model

            if (previousItem != null) {
                adbList.clearSelection()
                adbList.selectedIndex = devices.indexOf(previousItem)
            } else if (devices.isNotEmpty()) {
                adbList.selectedIndex = 0
            }

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