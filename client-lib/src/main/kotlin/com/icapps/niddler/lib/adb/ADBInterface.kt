package com.icapps.niddler.lib.adb

import com.google.gson.Gson
import com.icapps.niddler.lib.utils.createGsonListType
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.error
import com.icapps.niddler.lib.utils.logger
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

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

class ADBDevice(device: JadbDevice, private val bootstrap: ADBBootstrap) {

    private companion object {
        private const val ANNOUNCEMENT_PORT = 6394
        private const val REQUEST_QUERY = 0x01
        private val log = logger<ADBDevice>()
    }

    val serial: String = device.serial

    fun forwardTCPPort(localPort: Int, remotePort: Int) {
        bootstrap.executeADBCommand("-s", serial, "forward", "tcp:$localPort", "tcp:$remotePort")
    }

    fun removeTCPForward(localPort: Int) {
        bootstrap.executeADBCommand("-s", serial, "forward", "--remove", "$localPort")
    }

    fun getNiddlerSessions(): List<NiddlerSession>? {
        val freePort: Int
        try {
            val serverSocket = ServerSocket(0)
            freePort = serverSocket.localPort
            serverSocket.close()
            forwardTCPPort(freePort, ANNOUNCEMENT_PORT)
        } catch (e: IOException) {
            log.error("Failed to find and forward free port", e)
            return null
        }

        try {
            Socket(InetAddress.getByName("127.0.0.1"), freePort).use { socket ->
                socket.getOutputStream().apply {
                    write(REQUEST_QUERY)
                    flush()
                }
                return try {
                    val line = socket.getInputStream().bufferedReader().readLine()
                    line?.let {
                        val fromJson = Gson().fromJson<List<NiddlerAnnouncementMessage>>(line,
                                createGsonListType<NiddlerAnnouncementMessage>())
                        fromJson.map { NiddlerSession(this, it.packageName, it.port, it.pid, it.protocol) }
                    }
                } catch (e: IOException) {
                    log.debug("Failed to read announcement line", e)
                    null
                }
            }
        } finally {
            removeTCPForward(freePort)
        }
    }

}

data class NiddlerSession(val device: ADBDevice,
                          val packageName: String,
                          val port: Int,
                          val pid: Int,
                          val protocolVersion: Int)

private data class NiddlerAnnouncementMessage(
        val packageName: String,
        val port: Int,
        val pid: Int,
        val protocol: Int
)