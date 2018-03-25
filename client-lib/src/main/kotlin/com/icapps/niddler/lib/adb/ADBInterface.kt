package com.icapps.niddler.lib.adb

import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.error
import com.icapps.niddler.lib.utils.logger
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import java.lang.Exception

/**
 * @author nicolaverbeeck
 */
class ADBInterface(private val bootstrap: ADBBootstrap, private val connection: JadbConnection? = null) {

    private companion object {
        private val log = logger<ADBInterface>()
    }

    val devices: List<ADBDevice>
        get() = connection?.let {
            connection.devices.map {
                ADBDevice(it, bootstrap)
            }
        } ?: emptyList()

    fun createDeviceWatcher(deviceListener: (ADBInterface) -> Unit): Cancelable {
        val watcher = connection?.createDeviceWatcher(object : DeviceDetectionListener {
            override fun onException(e: Exception?) {
                log.error("Failed to watch for new devices", e)
            }

            override fun onDetect(devices: MutableList<JadbDevice>?) {
                log.debug("Device list updated")
                deviceListener.invoke(this@ADBInterface)
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

    private class CancellableWatcher(private val deviceWatcher: DeviceWatcher,
                                     private val thread: Thread) : Cancelable {
        override fun cancel() {
            deviceWatcher.stop()
            thread.interrupt()
        }
    }
}

class ADBDevice(private val device: JadbDevice, private val bootstrap: ADBBootstrap) {

    val serial = device.serial

    fun forwardTCPPort(localPort: Int, remotePort: Int) {
        val serial = device.serial
        if (serial != null)
            bootstrap.executeADBCommand("-s", serial, "forward", "tcp:$localPort", "tcp:$remotePort")
        else
            bootstrap.executeADBCommand("forward", "tcp:$localPort", "tcp:$remotePort")
    }

}