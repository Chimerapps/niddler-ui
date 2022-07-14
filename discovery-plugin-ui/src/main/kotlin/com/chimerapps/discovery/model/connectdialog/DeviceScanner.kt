package com.chimerapps.discovery.model.connectdialog

import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.debugbridge.DebugBridgeInterface
import com.chimerapps.discovery.device.idevice.IDeviceBootstrap
import com.chimerapps.discovery.device.local.LocalDevice
import com.chimerapps.discovery.model.connectdialog.android.AndroidModelCreator
import com.chimerapps.discovery.model.connectdialog.tizen.TizenModelCreator
import com.chimerapps.discovery.model.connectdialog.ios.IDeviceModelCreator
import com.chimerapps.discovery.model.connectdialog.local.LocalDeviceModelCreator
import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn
import com.intellij.openapi.application.ApplicationManager
import javax.swing.SwingWorker
import javax.swing.Timer

class DeviceScanner(
    private val adbInterface: DebugBridgeInterface,
    private val sdbInterface: DebugBridgeInterface,
    private val ideviceBootstrap: IDeviceBootstrap,
    private val announcementPort: Int, private val refreshDelay: Int = REFRESH_PROCESS_DELAY,
    private val listener: (List<DeviceModel>) -> Unit
) {

    private companion object {
        private const val REFRESH_PROCESS_DELAY = 4000
    }

    private var adbDeviceWatcher: DebugBridgeInterface.Cancelable? = null
    private var sdbDeviceWatcher: DebugBridgeInterface.Cancelable? = null
    private var scanningWorker: ScanningSwingWorker? = null
    private var refreshTimer: Timer? = null

    fun startScanning() {
        if (adbDeviceWatcher != null)
            throw IllegalStateException("Already scanning")

        adbDeviceWatcher = adbInterface.createDeviceWatcher(::onDebugBridgeDevicesUpdated)
        sdbDeviceWatcher = sdbInterface.createDeviceWatcher(::onDebugBridgeDevicesUpdated)
        scheduleDevicesRefresh()
    }

    fun stopScanning() {
        synchronized(this) {
            try {
                scanningWorker?.cancel(true)
            } catch (ignored: Throwable) {
            }
            scanningWorker = null

            refreshTimer?.stop()
            refreshTimer = null
        }
        adbDeviceWatcher?.cancel()
        adbDeviceWatcher = null
        sdbDeviceWatcher?.cancel()
        sdbDeviceWatcher = null
    }

    private fun scheduleDevicesRefresh() {
        synchronized(this) {
            try {
                scanningWorker?.cancel(true)
            } catch (ignored: Throwable) {
            }
            scanningWorker = ScanningSwingWorker(adbInterface, sdbInterface, ideviceBootstrap, announcementPort, listener).also { it.execute() }

            refreshTimer?.stop()
            refreshTimer = Timer(refreshDelay) {
                synchronized(this@DeviceScanner) {
                    try {
                        scanningWorker?.cancel(true)
                    } catch (ignored: Throwable) {
                    }
                    scanningWorker = ScanningSwingWorker(adbInterface, sdbInterface, ideviceBootstrap, announcementPort, listener).also { it.execute() }
                }
            }.also {
                it.isRepeats = true
                it.isCoalesce = false
                it.start()
            }
        }
    }

    private fun onDebugBridgeDevicesUpdated(@Suppress("UNUSED_PARAMETER") adbInterface: DebugBridgeInterface) {
        scheduleDevicesRefresh()
    }

}

private class ScanningSwingWorker(
    private val adbInterface: DebugBridgeInterface,
    private val sdbInterface: DebugBridgeInterface,
    private val iDeviceBootstrap: IDeviceBootstrap,
    private val announcementPort: Int,
    private val listener: (List<DeviceModel>) -> Unit
) : SwingWorker<List<DeviceModel>, Void>() {

    override fun doInBackground(): List<DeviceModel> {
        val localDevice = LocalDevice()

        val androidModelCreator = AndroidModelCreator(announcementPort)
        val tizenModelCreator = TizenModelCreator(announcementPort)
        val ideviceModelCreator = IDeviceModelCreator(announcementPort)
        val localDeviceModelCreator = LocalDeviceModelCreator()

        var adbDevices: List<DeviceModel>? = null
        var sdbDevices: List<DeviceModel>? = null
        var localDeviceSessions: List<DiscoveredSession>? = null
        var iDevices: List<DeviceModel>? = null

        val adbThread = Thread {
            adbDevices = adbInterface.devices.mapNotNull { device -> androidModelCreator.buildDeviceModel(device) }
        }.also { it.start() }

        val sdbThread = Thread {
            sdbDevices = sdbInterface.devices.mapNotNull { device -> tizenModelCreator.buildDeviceModel(device) }
        }.also { it.start() }

        val iDevicesThread = Thread {
            iDevices = try {
                iDeviceBootstrap.devices.mapNotNull { device -> ideviceModelCreator.buildDeviceModel(device) }
            } catch (e: Throwable) {
                logger<ScanningSwingWorker>().warn("Failed to get local sessions: ", e)
                null
            }
        }.also { it.start() }

        val localDevicesThread = Thread {
            localDeviceSessions = try {
                localDevice.getSessions(announcementPort)
            } catch (e: Throwable) {
                logger<ScanningSwingWorker>().warn("Failed to get local sessions: ", e)
                null
            }
        }.also { it.start() }

        try {
            adbThread.join()
            sdbThread.join()
            iDevicesThread.join()
            localDevicesThread.join()
        } catch (e: InterruptedException) {
            adbThread.interrupt()
            iDevicesThread.interrupt()
            localDevicesThread.interrupt()
        }

        val list = adbDevices.orEmpty().toMutableList()

        iDevices?.let(list::addAll)
        sdbDevices?.let(list::addAll)

        val capturedLocalSettings = localDeviceSessions
        if (capturedLocalSettings != null && capturedLocalSettings.isNotEmpty())
            list += localDeviceModelCreator.buildDeviceModel(localDevice, capturedLocalSettings)

        return list
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

}