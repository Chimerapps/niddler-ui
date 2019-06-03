package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.NiddlerMessageListenerAdapter
import com.icapps.niddler.lib.connection.model.NiddlerMessage

/**
 * @author nicolaverbeeck
 */
class NiddlerMessageContainer<T : NiddlerMessage>(val converter: (NiddlerMessage) -> T,
                                                  val storage: NiddlerMessageStorage<T>) {

    private val listeners: MutableSet<ParsedNiddlerMessageListener<T>> = hashSetOf()

    private val messageAdapter = object : NiddlerMessageListenerAdapter() {
        override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
            val parsedMessage = converter(niddlerMessage)
            storage.addMessage(parsedMessage)
            synchronized(listeners) {
                listeners.forEach { it.onMessage(parsedMessage) }
            }
        }
    }

    fun attach(client: NiddlerClient) {
        client.registerMessageListener(messageAdapter)
    }

    fun detach(client: NiddlerClient) {
        client.unregisterMessageListener(messageAdapter)
    }

    fun registerListener(listener: ParsedNiddlerMessageListener<T>) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: ParsedNiddlerMessageListener<T>) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }
}

interface ParsedNiddlerMessageListener<in T : NiddlerMessage> {

    fun onMessage(message: T)

}