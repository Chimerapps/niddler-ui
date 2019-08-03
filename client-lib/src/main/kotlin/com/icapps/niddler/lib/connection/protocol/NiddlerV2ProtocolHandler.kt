package com.icapps.niddler.lib.connection.protocol

import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn
import com.google.gson.JsonObject
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import org.java_websocket.client.WebSocketClient
import java.security.MessageDigest
import java.util.Base64

/**
 * @author Nicola Verbeeck
 */
open class NiddlerV2ProtocolHandler(messageListener: NiddlerMessageListener,
                                    protected val protocolVersion: Int) : NiddlerV1ProtocolHandler(messageListener) {

    companion object {
        private val log = logger<NiddlerV2ProtocolHandler>()
        const val MESSAGE_TYPE_SERVER_INFO = "serverInfo"
        const val MESSAGE_TYPE_AUTH_REQUEST = "authRequest"
        const val MESSAGE_TYPE_AUTH_AUTH_SUCCESS = "authSuccess"
        const val MESSAGE_TYPE_REQUEST = "request"
        const val MESSAGE_TYPE_RESPONSE = "response"
    }

    override fun onMessage(socket: WebSocketClient, message: JsonObject) {
        when (val type = message["type"].asString) {
            MESSAGE_TYPE_SERVER_INFO -> onServerInfo(message)
            MESSAGE_TYPE_AUTH_REQUEST -> onAuthRequest(socket, message)
            MESSAGE_TYPE_AUTH_AUTH_SUCCESS -> onAuthSuccess()
            MESSAGE_TYPE_REQUEST, MESSAGE_TYPE_RESPONSE -> onServiceMessage(message)
            else -> log.warn("Failed to handle message, unknown type: $type")
        }
    }

    private fun onAuthSuccess() {
        messageListener.onReady()
    }

    private fun onServerInfo(serverInfo: JsonObject) {
        messageListener.onServerInfo(
                NiddlerServerInfo(
                        serverName = serverInfo["serverName"].asString,
                        serverDescription = serverInfo["serverDescription"].asString,
                        icon = if (serverInfo["icon"]?.isJsonNull == false) serverInfo["icon"].asString else null,
                        protocol = protocolVersion))
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