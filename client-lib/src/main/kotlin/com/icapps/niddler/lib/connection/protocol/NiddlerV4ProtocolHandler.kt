package com.icapps.niddler.lib.connection.protocol

import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn
import com.google.gson.JsonObject
import com.icapps.niddler.lib.connection.StaticBlacklistEntry
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.debugger.model.MESSAGE_DEBUG_REPLY
import com.icapps.niddler.lib.debugger.model.MESSAGE_DEBUG_REQUEST
import com.icapps.niddler.lib.debugger.model.NiddlerDebugControlMessage
import com.icapps.niddler.lib.debugger.model.mergeToJson
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import org.java_websocket.client.WebSocketClient

/**
 * @author Nicola Verbeeck
 */
open class NiddlerV4ProtocolHandler(
    messageListener: NiddlerMessageListener,
    protected val debugListener: NiddlerDebugListener,
    protocolVersion: Int,
    private val messageStorage: NiddlerMessageContainer?
) : NiddlerV2ProtocolHandler(messageListener, protocolVersion) {

    companion object {
        private val log = logger<NiddlerV4ProtocolHandler>()
        const val MESSAGE_TYPE_DEBUG_REQUEST = "debugRequest"
        const val MESSAGE_TYPE_STATIC_BLACKLIST = "staticBlacklist"
    }

    override fun onMessage(socket: WebSocketClient, message: JsonObject) {
        when (message["type"].asString) {
            MESSAGE_TYPE_DEBUG_REQUEST -> onDebugRequest(socket, message)
            MESSAGE_TYPE_STATIC_BLACKLIST -> onStaticBlacklist(message)
            else -> super.onMessage(socket, message)
        }
    }

    private fun onDebugRequest(socket: WebSocketClient, message: JsonObject) {
        if (message.has("request")) {
            onDebugRequestOverride(socket, gson.fromJson(message.getAsJsonObject("request"), NiddlerMessage::class.java))
        } else if (message.has("requestId")) {
            val requestId = message.get("requestId").asString
            val messageId = message.get("messageId").asString
            if (message.has("response")) {
                onDebugResponseAction(socket, requestId, messageId, gson.fromJson(message.get("response"), NiddlerMessage::class.java))
            } else {
                onDebugRequestAction(socket, requestId, messageId)
            }
        } else {
            log.warn("Failed to debug request message, unknown type: $message")
        }
    }

    private fun onStaticBlacklist(message: JsonObject) {
        val id = message.get("id").asString
        val name = message.get("name").asString
        val newList = message.getAsJsonArray("entries")?.map {
            val entry = it.asJsonObject
            StaticBlacklistEntry(entry.get("pattern").asString, entry.get("enabled").asBoolean)
        }
        newList?.let { messageListener.onStaticBlacklistUpdated(id, name, it) }
    }

    private fun onDebugRequestOverride(socket: WebSocketClient, message: NiddlerMessage) {
        val response = debugListener.onRequestOverride(message)

        val controlMessage = NiddlerDebugControlMessage(MESSAGE_DEBUG_REQUEST, messageId = message.messageId, payload = response)

        socket.send(mergeToJson(gson, controlMessage).toString())
    }

    private fun onDebugRequestAction(socket: WebSocketClient, requestId: String, messageId: String) {
        val storage = messageStorage
        val response = debugListener.onRequestAction(requestId, request = storage?.let { storage.findRequest(requestId)?.let(storage::load) })

        val controlMessage = NiddlerDebugControlMessage(MESSAGE_DEBUG_REPLY, messageId = messageId, payload = response)

        socket.send(mergeToJson(gson, controlMessage).toString())
    }

    private fun onDebugResponseAction(socket: WebSocketClient, requestId: String, messageId: String, message: NiddlerMessage) {
        val storage = messageStorage
        val response = debugListener.onResponseAction(requestId, message, request = storage?.let { storage.findRequest(message.requestId)?.let(storage::load) })

        val controlMessage = NiddlerDebugControlMessage(MESSAGE_DEBUG_REPLY, messageId = messageId, payload = response)

        socket.send(mergeToJson(gson, controlMessage).toString())
    }

}