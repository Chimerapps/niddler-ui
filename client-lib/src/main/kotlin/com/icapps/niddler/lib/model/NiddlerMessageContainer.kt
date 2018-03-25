package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.NiddlerMessageListenerAdapter
import com.icapps.niddler.lib.connection.model.NiddlerMessage

/**
 * @author nicolaverbeeck
 */
class NiddlerMessageContainer(private val bodyParser: NiddlerMessageBodyParser,
                              val storage: NiddlerMessageStorage) {

    private val listeners: MutableSet<ParsedNiddlerMessageListener> = hashSetOf()

    private val messageAdapter = object : NiddlerMessageListenerAdapter() {
        override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
            val parsedMessage = bodyParser.parseBody(niddlerMessage) ?: return
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

    fun registerListener(listener: ParsedNiddlerMessageListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: ParsedNiddlerMessageListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }
}

interface ParsedNiddlerMessageListener {

    fun onMessage(message: ParsedNiddlerMessage)

}