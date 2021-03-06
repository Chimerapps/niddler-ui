package com.chimerapps.discovery.device.adb

import com.chimerapps.discovery.device.BaseDevice
import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.PreparedDeviceConnection
import com.chimerapps.discovery.utils.debug
import com.chimerapps.discovery.utils.error
import com.chimerapps.discovery.utils.freePort
import com.chimerapps.discovery.utils.logger
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice

/**
 * @author Nicola Verbeeck
 */
class ADBInterface(val bootstrap: ADBBootstrap, private val connection: JadbConnection? = null) {

    private companion object {
        private val log = logger<ADBInterface>()
    }

    val devices: List<ADBDevice>
        get() = connection?.let {
            connection.devices.map {
                ADBDevice(it, bootstrap)
            }
        } ?: emptyList()

    val isRealConnection: Boolean
        get() = connection != null

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
        bootstrap.executeADBCommand("-s", serial, "forward", "--remove", "tcp:$localPort")
    }

    fun executeADBCommand(vararg args: String): String? {
        val newArgs = Array(args.size + 2) { "" }
        newArgs[0] = "-s"
        newArgs[1] = serial
        System.arraycopy(args, 0, newArgs, 2, args.size)
        return bootstrap.executeADBCommand(*newArgs)
    }

    fun executeADBCommand(timeoutInSeconds: Long, vararg args: String): String? {
        val newArgs = Array(args.size + 2) { "" }
        newArgs[0] = "-s"
        newArgs[1] = serial
        System.arraycopy(args, 0, newArgs, 2, args.size)
        return bootstrap.executeADBCommand(timeoutInSeconds, *newArgs)
    }

    override fun getSessions(announcementPort: Int): List<DiscoveredSession> {
        val freePort = freePort()
        if (freePort <= 0) {
            return emptyList()
        }
        try {
            forwardTCPPort(freePort, announcementPort)
            return readAnnouncement(freePort)
        } finally {
            removeTCPForward(freePort)
        }
    }

    override fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection {
        forwardTCPPort(suggestedLocalPort, remotePort)
        return object : DirectPreparedConnection("127.0.0.1", suggestedLocalPort) {
            override fun tearDown() {
                try {
                    super.tearDown()
                } catch (ignore: Throwable) {
                }
                removeTCPForward(suggestedLocalPort)
            }
        }
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
