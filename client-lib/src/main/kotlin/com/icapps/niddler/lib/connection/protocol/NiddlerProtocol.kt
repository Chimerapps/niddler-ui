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
 * @date 22/11/16.
 */
interface NiddlerProtocol {

    fun onMessage(socket: WebSocketClient, message: JsonObject)

}

interface NiddlerMessageListener {

    fun onServerInfo(serverInfo: NiddlerServerInfo)

    fun onAuthRequest(): String?

    fun onServiceMessage(niddlerMessage: NiddlerMessage)

    fun onReady()

    fun onDebuggerAttached()

    fun onDebuggerActive()

    fun onDebuggerInactive()

    fun onClosed()

    fun onStaticBlacklistUpdated(id: String, name: String, entries: List<StaticBlacklistEntry>)

}

interface NiddlerDebugListener {

    fun onRequestOverride(message: NiddlerMessage): DebugRequest?

    fun onRequestAction(requestId: String): DebugResponse?

    fun onResponseAction(requestId: String, response: NiddlerMessage): DebugResponse?

}