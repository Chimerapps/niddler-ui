package com.icapps.niddler.lib.connection.protocol

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.icapps.niddler.lib.connection.model.NetworkNiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import org.java_websocket.client.WebSocketClient

/**
 * @author Nicola Verbeeck
 */
open class NiddlerV1ProtocolHandler(protected val messageListener: NiddlerMessageListener) : NiddlerProtocol {

    protected val gson: Gson = GsonBuilder()
            .registerTypeAdapterFactory(NiddlerMessageTypeAdapterFactory())
            .disableHtmlEscaping()
            .create()

    init {
        messageListener.onReady()
    }

    override fun onMessage(socket: WebSocketClient, message: JsonObject) {
        onServiceMessage(message)
    }

    protected fun onServiceMessage(message: JsonObject) {
        messageListener.onServiceMessage(gson.fromJson(message, NetworkNiddlerMessage::class.java))
    }

}

private class NiddlerMessageTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        @Suppress("UNCHECKED_CAST")
        if (type.type == NiddlerMessage::class.java)
            return gson.getAdapter(NetworkNiddlerMessage::class.java) as TypeAdapter<T>
        return null
    }
}
