package com.icapps.niddler.lib.connection

import com.chimerapps.discovery.utils.debug
import com.chimerapps.discovery.utils.logger
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.icapps.niddler.lib.connection.protocol.NiddlerProtocol
import com.icapps.niddler.lib.connection.protocol.NiddlerV1ProtocolHandler
import com.icapps.niddler.lib.connection.protocol.NiddlerV2ProtocolHandler
import com.icapps.niddler.lib.connection.protocol.NiddlerV4ProtocolHandler
import com.icapps.niddler.lib.debugger.NiddlerDebuggerConnection
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.io.Closeable
import java.net.URI
import java.util.HashSet

/**
 * @author Nicola Verbeeck
 */
class NiddlerClient(serverURI: URI, val withDebugger: Boolean, private val messageStorage: NiddlerMessageStorage<out NiddlerMessage>?) : Closeable,
        NiddlerMessageListener, NiddlerDebugListener, NiddlerDebuggerConnection {

    companion object {
        const val MESSAGE_TYPE_PROTOCOL = "protocol"
    }

    private val socketClient = WebSocketNiddlerClient(serverURI, this)

    internal val clientListeners: MutableSet<NiddlerMessageListener> = HashSet()
    @Volatile
    var debugListener: NiddlerDebugListener? = null
    internal var protocolHandler: NiddlerProtocol? = null

    val staticBlacklist = StaticBlacklistConfiguration()

    fun connect() {
        socketClient.connect()
    }

    override fun close() {
        socketClient.close()
    }

    fun registerMessageListener(listener: NiddlerMessageListener) {
        synchronized(clientListeners) {
            clientListeners.add(listener)
        }
        staticBlacklist.getConfiguration().forEach { listener.onStaticBlacklistUpdated(it.id, it.name, it.entries) }
    }

    fun unregisterMessageListener(listener: NiddlerMessageListener) {
        synchronized(clientListeners) {
            clientListeners.remove(listener)
        }
    }

    internal fun registerProtocolHandler(protocolVersion: Int) {
        protocolHandler = when (protocolVersion) {
            1 -> NiddlerV1ProtocolHandler(this)
            2, 3 -> NiddlerV2ProtocolHandler(this, protocolVersion)
            else -> NiddlerV4ProtocolHandler(messageListener = this, debugListener = this,
                    protocolVersion = protocolVersion, messageStorage = messageStorage)
        }
    }

    override fun onServerInfo(serverInfo: NiddlerServerInfo) {
        synchronized(clientListeners) {
            clientListeners.forEach { it.onServerInfo(serverInfo) }
        }
    }

    override fun onAuthRequest(): String? {
        synchronized(clientListeners) {
            clientListeners.forEach {
                val auth = it.onAuthRequest()
                if (auth != null)
                    return auth
            }
        }
        return null
    }

    override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
        synchronized(clientListeners) {
            clientListeners.forEach { it.onServiceMessage(niddlerMessage) }
        }
    }

    override fun onReady() {
        synchronized(clientListeners) {
            clientListeners.forEach { it.onReady() }
        }
    }

    override fun onClosed() {
        synchronized(clientListeners) {
            clientListeners.forEach { it.onClosed() }
        }
    }

    override fun onDebuggerAttached() {
        synchronized(clientListeners) {
            clientListeners.forEach { it.onDebuggerAttached() }
        }
    }

    override fun onDebuggerActive() {
        synchronized(clientListeners) {
            clientListeners.forEach { it.onDebuggerActive() }
        }
    }

    override fun onDebuggerInactive() {
        synchronized(clientListeners) {
            clientListeners.forEach { it.onDebuggerInactive() }
        }
    }

    override fun onRequestOverride(message: NiddlerMessage): DebugRequest? {
        return debugListener?.onRequestOverride(message)
    }

    override fun onRequestAction(requestId: String): DebugResponse? {
        return debugListener?.onRequestAction(requestId)
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        return debugListener?.onResponseAction(requestId, response, request)
    }

    override fun sendMessage(message: String) {
        socketClient.sendString(message)
    }

    override fun onStaticBlacklistUpdated(id: String, name: String, entries: List<StaticBlacklistEntry>) {
        staticBlacklist.onConfigurationUpdated(id, name, entries)
        synchronized(clientListeners) {
            clientListeners.forEach { it.onStaticBlacklistUpdated(id, name, entries) }
        }
    }

    fun setStaticBlacklistItemEnabled(id: String, pattern: String, enabled: Boolean) {
        val json = JsonObject()
        json.addProperty("type", "controlStaticBlacklist")
        json.addProperty("id", id)
        json.addProperty("pattern", pattern)
        json.addProperty("enabled", enabled)
        socketClient.sendString(json.toString())
    }

}

data class StaticBlacklistEntry(val pattern: String, val enabled: Boolean)

data class StaticBlacklistHandler(val id: String, val name: String, val entries: List<StaticBlacklistEntry>)

class StaticBlacklistConfiguration {

    private val configuration: MutableMap<String, StaticBlacklistHandler> = mutableMapOf()

    fun getConfiguration(): List<StaticBlacklistHandler> {
        synchronized(configuration) {
            return configuration.map { it.value }
        }
    }

    fun onConfigurationUpdated(id: String, name: String, entries: List<StaticBlacklistEntry>) {
        synchronized(configuration) {
            configuration[id] = StaticBlacklistHandler(id, name, entries)
        }
    }

    fun clear() {
        synchronized(configuration) {
            configuration.clear()
        }
    }
}

private class WebSocketNiddlerClient(serverURI: URI, private val parent: NiddlerClient) : WebSocketClient(serverURI, Draft_6455()) {

    companion object {
        private val log = logger<WebSocketNiddlerClient>()
    }

    override fun onOpen(handshakeData: ServerHandshake?) {
        log.debug("Connection succeeded: ${connection.remoteSocketAddress}")
        parent.staticBlacklist.clear()
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        log.debug("Connection closed: $reason")
        synchronized(parent.clientListeners) {
            parent.clientListeners.forEach { it.onClosed() }
        }
        parent.staticBlacklist.clear()
    }

    override fun onMessage(message: String) {
        log.debug("Got message: $message")
        val json = JsonParser().parse(message).asJsonObject
        val messageType = json.get("type").asString

        if (messageType == NiddlerClient.MESSAGE_TYPE_PROTOCOL) {
            parent.registerProtocolHandler(json.get("protocolVersion").asInt)
            return
        }
        parent.protocolHandler?.onMessage(this, json)
    }

    override fun onError(ex: Exception?) {
        log.debug(ex.toString())
    }

    fun sendString(data: String) {
        send(data)
    }
}