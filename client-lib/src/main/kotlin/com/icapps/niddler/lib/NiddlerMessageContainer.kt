package com.icapps.niddler.lib

import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.NiddlerMessageListenerAdapter
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageBodyParser
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessageListener

/**
 * @author nicolaverbeeck
 */
class NiddlerMessageContainer(private val bodyParser: NiddlerMessageBodyParser,
                              private val storage: NiddlerMessageStorage) {

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