package com.icapps.niddler.ui.connection

import com.google.gson.JsonObject
import com.icapps.niddler.ui.debugger.model.DebugRequest
import com.icapps.niddler.ui.debugger.model.DebugResponse
import com.icapps.niddler.ui.model.NiddlerMessage
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
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

}

interface NiddlerDebugListener {

    fun onRequestOverride(message: NiddlerMessage): DebugRequest?

    fun onRequestAction(messageId: String): DebugResponse?

    fun onResponseAction(messageId: String, response: NiddlerMessage): DebugResponse?

}