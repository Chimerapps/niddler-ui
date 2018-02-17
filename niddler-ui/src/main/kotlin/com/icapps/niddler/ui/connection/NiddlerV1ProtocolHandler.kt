package com.icapps.niddler.ui.connection

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.icapps.niddler.ui.model.NiddlerMessage
import org.java_websocket.client.WebSocketClient

/**
 * @author Nicola Verbeeck
 * @date 22/11/16.
 */
open class NiddlerV1ProtocolHandler(protected val messageListener: NiddlerMessageListener) : NiddlerProtocol {

    protected val gson: Gson = GsonBuilder()
            .disableHtmlEscaping()
            .create()

    init {
        messageListener.onReady()
    }

    override fun onMessage(socket: WebSocketClient, message: JsonObject) {
        onServiceMessage(message)
    }

    protected fun onServiceMessage(message: JsonObject) {
        messageListener.onServiceMessage(gson.fromJson(message, NiddlerMessage::class.java))
    }

}