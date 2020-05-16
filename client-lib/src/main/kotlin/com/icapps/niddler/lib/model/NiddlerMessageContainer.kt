package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
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
            val info = NiddlerMessageInfo.fromMessage(niddlerMessage)
            if (messagesChronological.addMessage(info)) {
                messagesLinked.addMessage(info)

                synchronized(internalStorages) {
                    internalStorages.forEach { it.addMessage(niddlerMessage) }
                }
                listeners.forEach { it.onServiceMessage(niddlerMessage) }
            }
        }
    }

    init {
        val (empty, nonEmpty) = storages.partition { it.isEmpty() }
        when {
            nonEmpty.isEmpty() -> {
            }
            nonEmpty.size == 1 -> {
                nonEmpty.first().allMessages().forEach {
                    empty.forEach { emptyStorage -> emptyStorage.addMessage(it) }
                    addMessageFromStorage(it)
                }
            }
            else -> {
                nonEmpty.first().allMessages().forEach(::addMessageFromStorage)
            }
        }
    }

    fun clear() {
        internalStorages.forEach(NiddlerMessageStorage::clear)
    }

    private fun addMessageFromStorage(niddlerMessage: NiddlerMessage) {
        val info = NiddlerMessageInfo.fromMessage(niddlerMessage)
        if (messagesChronological.addMessage(info)) {
            messagesLinked.addMessage(info)
        }
    }

    private fun getMessagesWithRequestId(requestId: String): List<NiddlerMessageInfo> {
        return synchronized(this) {
            messagesLinked[requestId] ?: emptyList()
        }
    }

    fun findResponse(message: NiddlerMessageInfo): NiddlerMessageInfo? {
        return getMessagesWithRequestId(message.requestId).find {
            !it.isRequest
        }
    }

    fun findRequest(message: NiddlerMessageInfo): NiddlerMessageInfo? {
        return findRequest(message.requestId)
    }

    fun findRequest(requestId: String): NiddlerMessageInfo? {
        return getMessagesWithRequestId(requestId).findLast(NiddlerMessageInfo::isRequest)
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
        synchronized(internalStorages) {
            internalStorages.forEach { (it as? Closeable)?.close() }
        }
    }

    fun isEmpty(): Boolean {
        return messagesChronological.isEmpty()
    }

    fun load(message: NiddlerMessageInfo): NiddlerMessage? {
        synchronized(internalStorages) {
            internalStorages.forEach { storage -> storage.loadMessage(message)?.let { return it } }
        }
        return null
    }

    fun loadHeaders(message: NiddlerMessageInfo): Map<String, List<String>>? {
        synchronized(internalStorages) {
            internalStorages.forEach { storage -> storage.loadMessageHeaders(message)?.let { return it } }
        }
        return null
    }

}