package com.chimerapps.discovery.device.debugbridge

import com.chimerapps.discovery.utils.debug
import com.chimerapps.discovery.utils.error
import com.chimerapps.discovery.utils.logger
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice

class DebugBridgeInterface(
    val bootstrap: DebugBridgeBootstrap,
    private val connection: JadbConnection? = null,
    private val deviceCreator: (JadbDevice, DebugBridgeBootstrap) -> DebugBridgeBasedDevice,
) {

    private companion object {
        private val log = logger<DebugBridgeInterface>()
    }

    val devices: List<DebugBridgeBasedDevice>
        get() = connection?.let {
            connection.devices.map {
                deviceCreator(it, bootstrap)
            }
        } ?: emptyList()

    val isRealConnection: Boolean
        get() = connection != null

    /**
     * Callbacks happen on a BACKGROUND THREAD
     */
    fun createDeviceWatcher(deviceListener: (DebugBridgeInterface) -> Unit): Cancelable {
        val watcher = connection?.createDeviceWatcher(object : DeviceDetectionListener {
            override fun onException(e: Exception?) {
                log.error("Failed to watch for new devices", e)
            }

            override fun onDetect(devices: MutableList<JadbDevice>?) {
                log.debug("Device list updated")
                deviceListener.invoke(this@DebugBridgeInterface)
            }
        })
        if (watcher != null) {
            val thread = Thread(watcher, "Device watcher").apply { start() }
            return CancellableWatcher(watcher, thread)
        }

        return object : Cancelable {
            override fun cancel() {
            }
        }
    }

    interface Cancelable {
        fun cancel()
    }

    private class CancellableWatcher(
        private val deviceWatcher: DeviceWatcher,
        private val thread: Thread
    ) : Cancelable {
        override fun cancel() {
            deviceWatcher.stop()
            thread.interrupt()
        }
    }
}