package com.icapps.niddler.lib.connection.protocol

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.icapps.niddler.lib.connection.model.NetworkNiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import org.java_websocket.client.WebSocketClient

/**
 * @author Nicola Verbeeck
 * @date 22/11/16.
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
