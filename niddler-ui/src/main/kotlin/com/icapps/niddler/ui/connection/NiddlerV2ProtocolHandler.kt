package com.icapps.niddler.ui.connection

import com.google.gson.JsonObject
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
import com.icapps.niddler.ui.util.logger
import org.java_websocket.client.WebSocketClient
import java.security.MessageDigest
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 22/11/16.
 */
open class NiddlerV2ProtocolHandler(messageListener: NiddlerMessageListener) : NiddlerV1ProtocolHandler(messageListener) {

    companion object {
        private val log = logger<NiddlerV2ProtocolHandler>()
    }

    override fun onMessage(socket: WebSocketClient, message: JsonObject) {
        val type = message["type"].asString

        when (type) {
            "serverInfo" -> onServerInfo(message)
            "authRequest" -> onAuthRequest(socket, message)
            "authSuccess" -> onAuthSuccess()
            "request", "response" -> onServiceMessage(message)
            else -> log.warn("Failed to handle message, unknown type: " + type)
        }
    }

    private fun onAuthSuccess() {
        messageListener.onReady()
    }

    private fun onServerInfo(serverInfo: JsonObject) {
        messageListener.onServerInfo(NiddlerServerInfo(serverInfo["serverName"].asString, serverInfo["serverDescription"].asString))
    }

    private fun onAuthRequest(socket: WebSocketClient, authRequestMessage: JsonObject) {
        val password = messageListener.onAuthRequest()
        if (password == null) {
            socket.close()
            return
        }
        val hash = calculateHash(authRequestMessage["hash"].asString, password)
        socket.send("{\"type\":\"authReply\",\"hashKey\":\"$hash\"}")
    }

    private fun calculateHash(hash: String, password: String): String {
        val bytes = (hash + password).toByteArray(Charsets.UTF_8)
        val hashedBytes = MessageDigest.getInstance("SHA-512").digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes)
    }

}