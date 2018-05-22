package com.icapps.niddler.lib.device

import com.google.gson.Gson
import com.icapps.niddler.lib.utils.createGsonListType
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.logger
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.URI
import java.util.*

/**
 * @author nicolaverbeeck
 */
interface Device {

    companion object {
        const val REQUEST_QUERY = 0x01
        const val ANNOUNCEMENT_PORT = 6394
    }

    fun getNiddlerSessions(): List<NiddlerSession>

    fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection

}

interface PreparedDeviceConnection {
    val uri: URI
}

data class NiddlerSession(val device: Device,
                          val packageName: String,
                          val port: Int,
                          val pid: Int,
                          val protocolVersion: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is NiddlerSession)
            return false

        return packageName == other.packageName
                && port == other.port
                && pid == other.pid
                && protocolVersion == other.protocolVersion
                && device == other.device
    }

    override fun hashCode(): Int {
        return Objects.hash(packageName, port, pid, protocolVersion, device)
    }
}

private data class NiddlerAnnouncementMessage(
        val packageName: String,
        val port: Int,
        val pid: Int,
        val protocol: Int
)

class DirectPreparedConnection(ip: String, port: Int) : PreparedDeviceConnection {

    override val uri: URI

    init {
        val tempUri = URI.create("sis://$ip")
        val usePort = if (tempUri.port == -1) port else tempUri.port

        uri = URI.create("ws://${tempUri.host}:$usePort")
    }

}

abstract class BaseDevice : Device {

    private companion object {
        private val log = logger<BaseDevice>()
    }

    private val gson = Gson()

    protected fun readAnnouncement(connectPort: Int): List<NiddlerSession> {
        Socket(InetAddress.getByName("127.0.0.1"), connectPort).use { socket ->
            socket.getOutputStream().apply {
                write(Device.REQUEST_QUERY)
                flush()
            }
            return try {
                val line = socket.getInputStream().bufferedReader().readLine()
                line?.let {
                    val fromJson = gson.fromJson<List<NiddlerAnnouncementMessage>>(line,
                            createGsonListType<NiddlerAnnouncementMessage>())
                    fromJson.map { NiddlerSession(this, it.packageName, it.port, it.pid, it.protocol) }
                } ?: emptyList()
            } catch (e: IOException) {
                log.debug("Failed to read announcement line", e)
                emptyList()
            }
        }
    }

}