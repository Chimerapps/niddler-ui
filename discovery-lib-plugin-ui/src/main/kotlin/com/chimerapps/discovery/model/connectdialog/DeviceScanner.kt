package com.chimerapps.discovery.model.connectdialog

import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.discovery.device.adb.ADBDevice
import com.chimerapps.discovery.device.adb.ADBInterface
import com.chimerapps.discovery.device.local.LocalDevice
import com.chimerapps.discovery.ui.IncludedLibIcons
import com.chimerapps.discovery.utils.localName
import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn
import com.icapps.tools.aec.EmulatorFactory
import com.intellij.openapi.application.ApplicationManager
import java.awt.event.ActionListener
import java.io.File
import javax.swing.Icon
import javax.swing.SwingWorker
import javax.swing.Timer

class DeviceScanner(private val adbInterface: ADBInterface, private val announcementPort: Int, private val listener: (List<DeviceModel>) -> Unit) {

    private companion object {
        private const val REFRESH_PROCESS_DELAY = 4000
    }

    private var deviceWatcher: ADBInterface.Cancelable? = null
    private var scanningWorker: ScanningSwingWorker? = null
    private var refreshTimer: Timer? = null

    fun startScanning() {
        if (deviceWatcher != null)
            throw IllegalStateException("Already scanning")

        deviceWatcher = adbInterface.createDeviceWatcher(::onAdbDevicesUpdated)
        scheduleAdbRefresh()
    }

    fun stopScanning() {
        synchronized(this) {
            try {
                scanningWorker?.cancel(true)
            } catch (e: Throwable) {
            }
            scanningWorker = null

            refreshTimer?.stop()
            refreshTimer = null
        }
        deviceWatcher?.cancel()
        deviceWatcher = null
    }

    private fun scheduleAdbRefresh() {
        synchronized(this) {
            try {
                scanningWorker?.cancel(true)
            } catch (e: Throwable) {
            }
            scanningWorker = ScanningSwingWorker(adbInterface, announcementPort, listener).also { it.execute() }

            refreshTimer?.stop()
            refreshTimer = Timer(REFRESH_PROCESS_DELAY, ActionListener {
                synchronized(this@DeviceScanner) {
                    try {
                        scanningWorker?.cancel(true)
                    } catch (e: Throwable) {
                    }
                    scanningWorker = ScanningSwingWorker(adbInterface, announcementPort, listener).also { it.execute() }
                }
            }).also {
                it.isRepeats = true
                it.isCoalesce = false
                it.start()
            }
        }
    }

    private fun onAdbDevicesUpdated(@Suppress("UNUSED_PARAMETER") adbInterface: ADBInterface) {
        scheduleAdbRefresh()
    }

}

private class ScanningSwingWorker(private val adbInterface: ADBInterface,
                                  private val announcementPort: Int,
                                  private val listener: (List<DeviceModel>) -> Unit)
    : SwingWorker<List<DeviceModel>, Void>() {

    private val authToken: String? = try {
        File("${System.getProperty("user.home")}/.emulator_console_auth_token").readText()
    } catch (e: Throwable) {
        null
    }

    override fun doInBackground(): List<DeviceModel> {
        val localDevice = LocalDevice()

        val adbDevices = adbInterface.devices.mapNotNull { device -> buildDeviceModel(device) }
        val localDeviceSessions = try {
            localDevice.getSessions(announcementPort)
        } catch (e: Throwable) {
            logger<ScanningSwingWorker>().warn("Failed to get local sessions: ", e)
            null
        }

        if (localDeviceSessions != null && localDeviceSessions.isNotEmpty())
            return adbDevices + buildDeviceModel(localDevice, localDeviceSessions)

        return adbDevices
    }

    override fun done() {
        super.done()

        if (ApplicationManager.getApplication().isDispatchThread) {
            dispatchNow()
        } else {
            ApplicationManager.getApplication().invokeLater(::dispatchNow)
        }
    }

    private fun dispatchNow() {
        if (isCancelled)
            return

        listener(get())
    }

    private fun buildDeviceModel(adbDevice: ADBDevice): DeviceModel? {
        @Suppress("LiftReturnOrAssignment")
        try {
            val bootstrap = adbInterface.bootstrap
            val serial = adbDevice.serial
            val emulated = bootstrap.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.characteristics") == "emulator"
            val name = getCorrectName(bootstrap, serial, emulated)
            val sdkVersion = bootstrap.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.sdk")
            val version = bootstrap.executeADBCommand("-s", serial, "shell", "getprop", "ro.build.version.release")
            val extraInfo = "(Android $version, API $sdkVersion)"

            return DeviceModel(name ?: "", extraInfo, getDeviceIcon(emulated),
                    serial, adbDevice, adbDevice.getSessions(announcementPort))
        } catch (e: Throwable) {
            logger<ScanningSwingWorker>().warn("Failed to build device model:", e)
            return null
        }
    }

    private fun buildDeviceModel(localDevice: LocalDevice, niddlerSessions: List<DiscoveredSession>): DeviceModel {
        return DeviceModel(localName, "", IncludedLibIcons.Devices.computer,
                "local", localDevice, niddlerSessions)
    }

    private fun getCorrectName(adb: ADBBootstrap, serial: String, emulated: Boolean): String? {
        return if (emulated) {
            val port = serial.split("-").last().toIntOrNull() ?: return getName(adb, serial)
            if (authToken.isNullOrBlank())
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

    fun getDeviceIcon(emulator: Boolean): Icon {
        return if (emulator) IncludedLibIcons.Devices.emulator else IncludedLibIcons.Devices.real
    }
}