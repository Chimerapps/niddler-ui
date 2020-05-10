package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.icapps.niddler.lib.model.storage.InMemoryNiddlerMessageStorage
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import java.io.Closeable

/**
 * @author Nicola Verbeeck
 */
class NiddlerMessageContainer(vararg storages: NiddlerMessageStorage) {

    private val internalStorages = storages

    private val listeners: MutableSet<NiddlerMessageListener> = hashSetOf()

    val messagesChronological: ObservableChronologicalMessageList = ObservableChronologicalMessageList(this)
    val messagesLinked = ObservableLinkedMessageList(this)

    private val messageAdapter = object : NiddlerMessageListener {
        override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
            if (messagesChronological.addMessage(niddlerMessage)) {
                messagesLinked.addMessage(niddlerMessage)

                storages.forEach { it.addMessage(niddlerMessage) }
                listeners.forEach { it.onServiceMessage(niddlerMessage) }
            }
        }
    }

    init {
        val (empty, nonEmpty) = storages.partition { it.isEmpty() }
        when {
            nonEmpty.isEmpty() -> {}
            nonEmpty.size == 1 -> {
                nonEmpty.first().allMessages().forEach {
                    empty.forEach { emptyStorage -> emptyStorage.addMessage(it) }
                    addMessage(it)
                }
            }
            else -> {
                nonEmpty.first().allMessages().forEach(::addMessage)
            }
        }
    }

    fun clear() {
        internalStorages.forEach(NiddlerMessageStorage::clear)
    }

    private fun addMessage(niddlerMessage: NiddlerMessage) {
        if (messagesChronological.addMessage(niddlerMessage)) {
            messagesLinked.addMessage(niddlerMessage)
        }
    }

    private fun getMessagesWithRequestId(requestId: String): List<NiddlerMessage> {
        return synchronized(this) {
            messagesLinked[requestId] ?: emptyList()
        }
    }

    fun findResponse(message: NiddlerMessage): NiddlerMessage? {
        return getMessagesWithRequestId(message.requestId).find {
            !it.isRequest
        }
    }

    fun findRequest(message: NiddlerMessage): NiddlerMessage? {
        return findRequest(message.requestId)
    }

    fun findRequest(requestId: String): NiddlerMessage? {
        return getMessagesWithRequestId(requestId).findLast(NiddlerMessage::isRequest)
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

    fun close() {
        internalStorages.forEach { (it as? Closeable)?.close() }
    }

    fun isEmpty(): Boolean {
        return messagesChronological.isEmpty()
    }
}