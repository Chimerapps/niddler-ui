package com.icapps.niddler.ui

import com.google.gson.JsonParser
import com.icapps.niddler.ui.connection.NiddlerMessageListener
import com.icapps.niddler.ui.connection.NiddlerProtocol
import com.icapps.niddler.ui.connection.NiddlerV1ProtocolHandler
import com.icapps.niddler.ui.connection.NiddlerV2ProtocolHandler
import com.icapps.niddler.ui.model.NiddlerMessage
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
import com.icapps.niddler.ui.util.logger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 14/11/2016.
 */
class NiddlerClient(serverURI: URI?) : WebSocketClient(serverURI, Draft_17()), NiddlerMessageListener {

    companion object {
        private val log = logger<NiddlerClient>()
    }

    private val clientListeners: MutableSet<NiddlerMessageListener> = HashSet()
    private var protocolHandler: NiddlerProtocol? = null

    override fun onOpen(handshakeData: ServerHandshake?) {
        log.debug("Connection succeeded: ${connection.remoteSocketAddress}")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        log.debug("Connection closed: $reason")
        synchronized(clientListeners) {
            clientListeners.forEach { it.onClosed() }
        }
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
}
