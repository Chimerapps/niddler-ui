package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage

/**
 * @author Nicola Verbeeck
 */
class NiddlerMessageContainer(val storage: NiddlerMessageStorage,
                              val secondaryStorage: NiddlerMessageStorage) {

    private val listeners: MutableSet<NiddlerMessageListener> = hashSetOf()

    private val messageAdapter = object : NiddlerMessageListener {
        override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
            storage.addMessage(niddlerMessage)
            secondaryStorage.addMessage(niddlerMessage)
            listeners.forEach { it.onServiceMessage(niddlerMessage) }
        }
    }

    fun attach(client: NiddlerClient) {
        client.registerMessageListener(messageAdapter)
    }

    fun detach(client: NiddlerClient) {
        client.unregisterMessageListener(messageAdapter)
    }

    fun registerListener(listener: NiddlerMessageListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: NiddlerMessageListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }
}