package com.icapps.niddler.ui

import com.google.gson.JsonParser
import com.icapps.niddler.ui.connection.NiddlerMessageListener
import com.icapps.niddler.ui.connection.NiddlerProtocol
import com.icapps.niddler.ui.connection.NiddlerV1ProtocolHandler
import com.icapps.niddler.ui.connection.NiddlerV2ProtocolHandler
import com.icapps.niddler.ui.model.NiddlerMessage
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import trikita.log.Log
import java.net.URI
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 14/11/2016.
 */
class NiddlerClient(serverURI: URI?) : WebSocketClient(serverURI, Draft_17()), NiddlerMessageListener {

    private val clientListeners: MutableSet<NiddlerMessageListener> = HashSet()
    private var protocolHandler: NiddlerProtocol? = null

    override fun onOpen(handshakeData: ServerHandshake?) {
        Log.d("Connection succeeded: " + connection.remoteSocketAddress)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("Connection closed: " + reason)
        synchronized(clientListeners) {
            clientListeners.forEach { it.onClosed() }
        }
    }

    override fun onMessage(message: String) {
        Log.d("Got message: " + message)
        val json = JsonParser().parse(message).asJsonObject
        val messageType = json.get("type").asString

        if (messageType == "protocol") {
            registerProtocolHandler(json.get("protocolVersion").asInt)
            return
        }
        protocolHandler?.onMessage(this, json)
    }

    override fun onError(ex: Exception?) {
        Log.d(ex.toString())
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
            2 -> protocolHandler = NiddlerV2ProtocolHandler(this)
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
