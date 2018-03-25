package com.icapps.niddler.lib.connection.protocol

import com.google.gson.JsonObject
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.debugger.model.DebugReplyPayload
import com.icapps.niddler.lib.debugger.model.MESSAGE_DEBUG_REPLY
import com.icapps.niddler.lib.debugger.model.NiddlerDebugControlMessage
import com.icapps.niddler.lib.debugger.model.mergeToJson
import com.icapps.niddler.lib.utils.logger
import com.icapps.niddler.lib.utils.warn
import org.java_websocket.client.WebSocketClient

/**
 * @author Nicola Verbeeck
 * @date 22/11/16.
 */
open class NiddlerV4ProtocolHandler(messageListener: NiddlerMessageListener,
                                    protected val debugListener: NiddlerDebugListener,
                                    protocolVersion: Int) : NiddlerV2ProtocolHandler(messageListener, protocolVersion) {

    companion object {
        private val log = logger<NiddlerV4ProtocolHandler>()
    }

    override fun onMessage(socket: WebSocketClient, message: JsonObject) {
        val type = message["type"].asString

        when (type) {
            "debugRequest" -> onDebugRequest(socket, message)
            else -> super.onMessage(socket, message)
        }
    }

    private fun onDebugRequest(socket: WebSocketClient, message: JsonObject) {
        if (message.has("request")) {
            onDebugRequestOverride(socket, gson.fromJson(message, NiddlerMessage::class.java))
        } else if (message.has("requestId")) {
            val requestId = message.get("requestId").asString
            if (message.has("response")) {
                onDebugResponseAction(socket, requestId, gson.fromJson(message, NiddlerMessage::class.java))
            } else {
                onDebugRequestAction(socket, requestId)
            }
        } else {
            log.warn("Failed to debug request message, unknown type: $message")
        }
    }

    private fun onDebugRequestOverride(socket: WebSocketClient, message: NiddlerMessage) {
        val response = debugListener.onRequestOverride(message)

        val messageIdPayload = DebugReplyPayload(message.messageId)
        val controlMessage = NiddlerDebugControlMessage(MESSAGE_DEBUG_REPLY, response)

        socket.send(mergeToJson(gson, messageIdPayload, controlMessage).toString())
    }

    private fun onDebugRequestAction(socket: WebSocketClient, requestId: String) {
        val response = debugListener.onRequestAction(requestId)

        val messageIdPayload = DebugReplyPayload(requestId)
        val controlMessage = NiddlerDebugControlMessage(MESSAGE_DEBUG_REPLY, response)

        socket.send(mergeToJson(gson, messageIdPayload, controlMessage).toString())
    }

    private fun onDebugResponseAction(socket: WebSocketClient, requestId: String, message: NiddlerMessage) {
        val response = debugListener.onResponseAction(requestId, message)

        val messageIdPayload = DebugReplyPayload(requestId)
        val controlMessage = NiddlerDebugControlMessage(MESSAGE_DEBUG_REPLY, response)

        socket.send(mergeToJson(gson, messageIdPayload, controlMessage).toString())
    }

}