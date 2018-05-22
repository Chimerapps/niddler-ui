package com.icapps.niddler.lib.device.adb

import com.icapps.niddler.lib.device.BaseDevice
import com.icapps.niddler.lib.device.Device
import com.icapps.niddler.lib.device.DirectPreparedConnection
import com.icapps.niddler.lib.device.NiddlerSession
import com.icapps.niddler.lib.device.PreparedDeviceConnection
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.error
import com.icapps.niddler.lib.utils.logger
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import java.io.IOException
import java.lang.Exception
import java.net.ServerSocket

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

    /**
     * Callbacks happen on a BACKGROUND THREAD
     */
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

class ADBDevice(device: JadbDevice, private val bootstrap: ADBBootstrap) : BaseDevice() {

    private companion object {
        private val log = logger<ADBDevice>()
    }

    val serial: String = device.serial

    fun forwardTCPPort(localPort: Int, remotePort: Int) {
        bootstrap.executeADBCommand("-s", serial, "forward", "tcp:$localPort", "tcp:$remotePort")
    }

    fun removeTCPForward(localPort: Int) {
        bootstrap.executeADBCommand("-s", serial, "forward", "--remove", "$localPort")
    }

    override fun getNiddlerSessions(): List<NiddlerSession> {
        val freePort: Int
        try {
            val serverSocket = ServerSocket(0)
            freePort = serverSocket.localPort
            serverSocket.close()
            forwardTCPPort(freePort, Device.ANNOUNCEMENT_PORT)
        } catch (e: IOException) {
            log.error("Failed to find and forward free port", e)
            return emptyList()
        }

        try {
            return readAnnouncement(freePort)
        } finally {
            removeTCPForward(freePort)
        }
    }

    override fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection {
        forwardTCPPort(suggestedLocalPort, remotePort)
        return DirectPreparedConnection("127.0.0.1", suggestedLocalPort)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ADBDevice

        if (serial != other.serial) return false

        return true
    }

    override fun hashCode(): Int {
        return serial.hashCode()
    }

}
