package com.icapps.niddler.ui.connection

import com.google.gson.JsonObject
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

    fun onClosed()

}