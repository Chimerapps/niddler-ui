package com.icapps.niddler.lib.connection

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
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.logger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.HashSet

/**
 * @author Nicola Verbeeck
 * @date 14/11/2016.
 */
class NiddlerClient(serverURI: URI, val withDebugger: Boolean) : WebSocketClient(serverURI, Draft_6455()),
        NiddlerMessageListener, NiddlerDebugListener, NiddlerDebuggerConnection {

    companion object {
        private val log = logger<NiddlerClient>()
    }

    private val clientListeners: MutableSet<NiddlerMessageListener> = HashSet()
    @Volatile
    var debugListener: NiddlerDebugListener? = null
    private var protocolHandler: NiddlerProtocol? = null

    var staticBlacklist: List<Pair<String, Boolean>> = emptyList()
        private set

    override fun onOpen(handshakeData: ServerHandshake?) {
        log.debug("Connection succeeded: ${connection.remoteSocketAddress}")
        staticBlacklist = emptyList()
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        log.debug("Connection closed: $reason")
        synchronized(clientListeners) {
            clientListeners.forEach { it.onClosed() }
        }
        staticBlacklist = emptyList()
    }

    override fun onMessage(message: String) {
        log.debug("Got message: $message")
        val json = JsonParser().parse(message).asJsonObject
        val messageType = json.get("type").asString

        if (messageType == "protocol") {
            registerProtocolHandler(json.get("protocolVersion").asInt)
            return
        }
        protocolHandler?.onMessage(this, json)
    }

    override fun onError(ex: Exception?) {
        log.debug(ex.toString())
    }

    fun registerMessageListener(listener: NiddlerMessageListener) {
        synchronized(clientListeners) {
            clientListeners.add(listener)
        }
        listener.onStaticBlacklistUpdated(staticBlacklist)
    }

    fun unregisterMessageListener(listener: NiddlerMessageListener) {
        synchronized(clientListeners) {
            clientListeners.remove(listener)
        }
    }

    private fun registerProtocolHandler(protocolVersion: Int) {
        when (protocolVersion) {
            1 -> protocolHandler = NiddlerV1ProtocolHandler(this)
            2, 3 -> protocolHandler = NiddlerV2ProtocolHandler(this, protocolVersion)
            else -> protocolHandler = NiddlerV4ProtocolHandler(messageListener = this, debugListener = this,
                    protocolVersion = protocolVersion)
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

    override fun onResponseAction(requestId: String, response: NiddlerMessage): DebugResponse? {
        return debugListener?.onResponseAction(requestId, response)
    }

    override fun sendMessage(message: String) {
        send(message)
    }

    override fun onStaticBlacklistUpdated(entries: List<Pair<String, Boolean>>) {
        staticBlacklist = entries
        synchronized(clientListeners) {
            clientListeners.forEach { it.onStaticBlacklistUpdated(staticBlacklist) }
        }
    }

    fun setStaticBlacklistItemEnabled(pattern: String, enabled: Boolean) {
        val json = JsonObject();
        json.addProperty("type","controlStaticBlacklist")
        json.addProperty("pattern", pattern)
        json.addProperty("enabled", enabled)
        send(json.toString())
    }

}