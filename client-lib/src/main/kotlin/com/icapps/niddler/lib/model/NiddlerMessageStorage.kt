package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage

/**
 * @author Nicola Verbeeck
 */
interface NiddlerMessageStorage<T : NiddlerMessage> {

    val messagesChronological: ObservableChronologicalMessageList<T>
    val messagesLinked: Map<String, List<T>>

    fun messagesLinkedWithFilter(filter: Filter<T>?): Map<String, List<T>>

    fun addMessage(message: T)

    fun getMessagesWithRequestId(requestId: String): List<T>

    fun findResponse(message: T): T?

    fun findRequest(message: T): T?

    fun clear()

    interface Filter<T : NiddlerMessage> {

        fun messageFilter(message: T, storage: NiddlerMessageStorage<T>): Boolean

        fun messageFilter(relatedMessages: List<T>): List<T>
    }

}

class InMemoryNiddlerMessageStorage<T : NiddlerMessage> : NiddlerMessageStorage<T> {

    private val messagesMapped = SemiOrderedMappingStorage<T>()

    override val messagesChronological: ObservableChronologicalMessageList<T> = ObservableChronologicalMessageList(this)

    override val messagesLinked: Map<String, List<T>>
        get() = messagesLinkedWithFilter(null)

    override fun clear() {
        synchronized(this) {
            messagesChronological.clear()
            messagesMapped.clear()
        }
    }

    override fun addMessage(message: T) {
        synchronized(this) {
            if (messagesChronological.addMessage(message))
                messagesMapped.add(message.requestId, message)
        }
    }

    override fun getMessagesWithRequestId(requestId: String): List<T> {
        return synchronized(this) {
            messagesMapped[requestId] ?: emptyList()
        }
    }

    override fun findResponse(message: T): T? {
        return getMessagesWithRequestId(message.requestId).find {
            !it.isRequest
        }
    }

    override fun findRequest(message: T): T? {
        return getMessagesWithRequestId(message.requestId).find(NiddlerMessage::isRequest)
    }

    override fun messagesLinkedWithFilter(filter: NiddlerMessageStorage.Filter<T>?)
            : Map<String, List<T>> {
        if (filter == null) {
            return synchronized(this) {
                messagesMapped.toMap()
            }
        }

        return synchronized(this) {
            messagesMapped.toMap(filter)
        }
    }
}

private class SemiOrderedMappingStorage<T : NiddlerMessage> {

    private val data = mutableListOf<MappingEntry<T>>()

    fun add(key: String, message: T) {
        val toFind = data.lastOrNull { it.key == key }
        if (toFind == null) {
            data += MappingEntry(message.timestamp, key, mutableListOf(message))
            data.sortBy(MappingEntry<T>::time)
            return
        }
        toFind.items += message
        toFind.items.sortBy(NiddlerMessage::timestamp)
        if (toFind.time > message.timestamp) {
            toFind.time = message.timestamp
            data.sortBy(MappingEntry<T>::time)
        }
    }

    fun clear() {
        data.clear()
    }

    operator fun get(key: String): List<T>? {
        return data.find { it.key == key }?.items
    }

    fun toMap(): Map<String, List<T>> {
        val map = LinkedHashMap<String, List<T>>()
        data.forEach {
            map[it.key] = ArrayList(it.items)
        }
        return map
    }

    fun toMap(filter: NiddlerMessageStorage.Filter<T>): Map<String, List<T>> {
        val map = LinkedHashMap<String, List<T>>()
        data.forEach {
            val copy = filter.messageFilter(it.items)
            if (copy.isNotEmpty())
                map[it.key] = copy
        }
        return map
    }
}

private data class MappingEntry<T : NiddlerMessage>(var time: Long, val key: String, val items: MutableList<T>)