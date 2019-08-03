package com.chimerapps.discovery.device

import com.chimerapps.discovery.utils.createGsonListType
import com.chimerapps.discovery.utils.debug
import com.chimerapps.discovery.utils.logger
import com.google.gson.Gson
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.util.Objects

/**
 * @author Nicola Verbeeck
 */
interface Device {

    companion object {
        const val REQUEST_QUERY = 0x01
        const val ANNOUNCEMENT_PORT = 6394
    }

    fun getSessions(): List<DiscoveredSession>

    fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection

}

interface PreparedDeviceConnection {
    val uri: URI

    fun tearDown()
}

data class DiscoveredSession(val device: Device,
                             val packageName: String,
                             val port: Int,
                             val pid: Int,
                             val protocolVersion: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is DiscoveredSession)
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

private data class DiscoveryAnnouncementMessage(
        val packageName: String,
        val port: Int,
        val pid: Int,
        val protocol: Int
)

open class DirectPreparedConnection(ip: String, port: Int) : PreparedDeviceConnection {

    final override val uri: URI

    init {
        val tempUri = URI.create("sis://$ip")
        val usePort = if (tempUri.port == -1) port else tempUri.port

        uri = URI.create("ws://${tempUri.host}:$usePort")
    }

    override fun tearDown() {
        //No-op
    }
}

abstract class BaseDevice : Device {

    private companion object {
        private val log = logger<BaseDevice>()
        private const val MAX_TIMEOUT = 300
        private const val MAX_READ_TIMEOUT = 1000
    }

    private val gson = Gson()

    protected fun readAnnouncement(connectPort: Int): List<DiscoveredSession> {
        Socket().use { socket ->
            try {
                socket.connect(InetSocketAddress(InetAddress.getLoopbackAddress(), connectPort), MAX_TIMEOUT)
                socket.soTimeout = MAX_READ_TIMEOUT
            } catch (e: ConnectException) {
                return emptyList()
            }

            socket.getOutputStream().also { stream ->
                stream.write(Device.REQUEST_QUERY)
                stream.flush()
            }
            return try {
                val line = socket.getInputStream().bufferedReader().readLine()
                line?.let {
                    val fromJson = gson.fromJson<List<DiscoveryAnnouncementMessage>>(line, createGsonListType<DiscoveryAnnouncementMessage>())
                    fromJson.map { DiscoveredSession(this, it.packageName, it.port, it.pid, it.protocol) }
                } ?: emptyList()
            } catch (e: IOException) {
                log.debug("Failed to read announcement line", e)
                emptyList()
            }
        }
    }

}