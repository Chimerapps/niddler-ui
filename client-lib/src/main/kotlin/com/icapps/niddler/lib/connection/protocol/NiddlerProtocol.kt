package com.icapps.niddler.lib.connection.protocol

import com.google.gson.JsonObject
import com.icapps.niddler.lib.connection.StaticBlacklistEntry
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import org.java_websocket.client.WebSocketClient

/**
 * @author Nicola Verbeeck
 */
interface NiddlerProtocol {

    /**
     * Called when a new websocket message has been received
     *
     * @param socket    The websocket client
     * @param message   The 'parsed' message contained in the websocket message
     */
    fun onMessage(socket: WebSocketClient, message: JsonObject)

}

/**
 * Listener for niddler messages
 */
interface NiddlerMessageListener {

    /**
     * Called when the server information has been received
     *
     * @param serverInfo    Server information for the connection
     */
    fun onServerInfo(serverInfo: NiddlerServerInfo) {}

    /**
     * Called when the server requests password based authentication
     *
     * @return The pre-shared password to authenticate to the server
     */
    fun onAuthRequest(): String? = null

    /**
     * Called when a new data message has been received
     *
     * @param niddlerMessage    The message
     */
    fun onServiceMessage(niddlerMessage: NiddlerMessage) {}

    /**
     * Called when the connection is ready to receive data messages. This will be called after the authentication succeeds or directly after connecting if the server does not
     * request authentication
     */
    fun onReady() {}

    /**
     * Called when the debugger as attached to the remote process
     */
    fun onDebuggerAttached() {}

    /**
     * Called when the debugger is activated
     */
    fun onDebuggerActive() {}

    /**
     * Called when the debugger is no longer active
     */
    fun onDebuggerInactive() {}

    /**
     * Called when the connection with the server has been closed
     */
    fun onClosed() {}

    /**
     * Called when the static blacklist has been updated
     *
     * @param id    The id of the remote niddler instance
     * @param name  The name of the remote niddler instance
     * @param entries   The entries in the blacklist for the remote instance
     */
    fun onStaticBlacklistUpdated(id: String, name: String, entries: List<StaticBlacklistEntry>) {}

}

/**
 * Listener for niddler debug requests
 */
interface NiddlerDebugListener {

    /**
     * Called when the server has hit a breakpoint which allows us to replace the request before it is sent to the actual network server
     *
     * @param actionId  The action id of the debug action as sent during the configuration stage
     * @param message   The message containing the intercepted request
     * @return The message to send to the network server instead. Return null to skip overwriting the request
     */
    fun onRequestOverride(actionId: String, message: NiddlerMessage): DebugRequest?

    /**
     * Called when the server has hit a configured breakpoint for a request and allows the client to return a request without hitting the actual network server
     *
     * @param actionId  The action id of the debug action as sent during the configuration stage
     * @param requestId The id ({@link NiddlerMessage.requestId}) of the request that triggered the breakpoint
     * @return The overridden response to send to the server instead of hitting the actual network server. Return null to allow normal processing to continue
     */
    fun onRequestAction(actionId: String, requestId: String): DebugResponse?

    /**
     * Called when the server has hit a configured breakpoint for a request when the response has been received from the actual network server
     *
     * @param actionId  The action id of the debug action as sent during the configuration stage
     * @param requestId The id ({@link NiddlerMessage.requestId}) of the request that triggered the breakpoint
     * @param response  The response from the actual network server
     * @param request If available, the request that triggered this response action
     * @return The overridden response to send to the server instead of returning what the actual network server returned. Return null to use the actual network server's provided
     * response
     */
    fun onResponseAction(actionId: String, requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse?

}