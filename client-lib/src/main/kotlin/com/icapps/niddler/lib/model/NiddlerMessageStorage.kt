package com.icapps.niddler.lib.model

/**
 * @author nicolaverbeeck
 */
interface NiddlerMessageStorage {

    val messagesChronological: List<ParsedNiddlerMessage>
    val messagesLinked: Map<String, List<ParsedNiddlerMessage>>
    var filter: Filter?

    fun messagesLinkedWithFilter(filter: Filter?): Map<String, List<ParsedNiddlerMessage>>

    fun addMessage(message: ParsedNiddlerMessage)

    fun getMessagesWithRequestId(requestId: String): List<ParsedNiddlerMessage>

    fun findResponse(message: ParsedNiddlerMessage): ParsedNiddlerMessage?

    fun findRequest(message: ParsedNiddlerMessage): ParsedNiddlerMessage?

    fun clear()

    interface Filter {

        fun messageFilter(message: ParsedNiddlerMessage, storage: NiddlerMessageStorage): Boolean

        fun messageFilter(relatedMessages: List<ParsedNiddlerMessage>): List<ParsedNiddlerMessage>
    }

}

class InMemoryNiddlerMessageStorage : NiddlerMessageStorage {

    private val messagesList: MutableList<ParsedNiddlerMessage> = arrayListOf()
    private val messagesMapped = SemiOrderedMappingStorage()

    override val messagesChronological: List<ParsedNiddlerMessage>
        get() = synchronized(messagesList) { ArrayList(messagesList) }

    override val messagesLinked: Map<String, List<ParsedNiddlerMessage>>
        get() = messagesLinkedWithFilter(filter)

    override var filter: NiddlerMessageStorage.Filter? = null
        set(value) {
            synchronized(messagesList) {
                field = value
            }
        }
        get() = synchronized(messagesList) { field }

    override fun clear() {
        synchronized(messagesList) {
            messagesList.clear()
            messagesMapped.clear()
        }
    }

    override fun addMessage(message: ParsedNiddlerMessage) {
        synchronized(messagesList) {
            val size = messagesList.size
            var insertIndex = 0
            var duplicateTimestamps = false
            for (i in 0 until size) {
                val item = messagesList[size - i - 1]
                if (item.timestamp < message.timestamp) {
                    insertIndex = size - i
                    break
                } else if (item.timestamp == message.timestamp) {
                    duplicateTimestamps = true
                    insertIndex = size - i
                    break
                }
            }
            if (duplicateTimestamps && messagesList.indexOfFirst { it.messageId == message.messageId } != -1) {
                return
            }
            messagesList.add(insertIndex, message)
            messagesMapped.add(message.requestId, message)
        }
    }

    override fun getMessagesWithRequestId(requestId: String): List<ParsedNiddlerMessage> {
        return synchronized(messagesList) {
            messagesMapped[requestId] ?: emptyList()
        }
    }

    override fun findResponse(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return getMessagesWithRequestId(message.requestId).find {
            !it.isRequest
        }
    }

    override fun findRequest(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return getMessagesWithRequestId(message.requestId).find(ParsedNiddlerMessage::isRequest)
    }

    override fun messagesLinkedWithFilter(filter: NiddlerMessageStorage.Filter?)
            : Map<String, List<ParsedNiddlerMessage>> {
        if (filter == null) {
            return synchronized(messagesList) {
                messagesMapped.toMap()
            }
        }

        return synchronized(messagesList) {
            messagesMapped.toMap(filter)
        }
    }
}

private class SemiOrderedMappingStorage {

    private val data = mutableListOf<MappingEntry>()

    fun add(key: String, message: ParsedNiddlerMessage) {
        val toFind = data.lastOrNull { it.key == key }
        if (toFind == null) {
            data += MappingEntry(message.timestamp, key, mutableListOf(message))
            data.sortBy(MappingEntry::time)
            return
        }
        toFind.items += message
        toFind.items.sortBy(ParsedNiddlerMessage::timestamp)
        if (toFind.time > message.timestamp) {
            toFind.time = message.timestamp
            data.sortBy(MappingEntry::time)
        }
    }

    fun clear() {
        data.clear()
    }

    operator fun get(key: String): List<ParsedNiddlerMessage>? {
        return data.find { it.key == key }?.items
    }

    fun toMap(): Map<String, List<ParsedNiddlerMessage>> {
        val map = LinkedHashMap<String, List<ParsedNiddlerMessage>>()
        data.forEach {
            map[it.key] = ArrayList(it.items)
        }
        return map
    }

    fun toMap(filter: NiddlerMessageStorage.Filter): Map<String, List<ParsedNiddlerMessage>> {
        val map = LinkedHashMap<String, List<ParsedNiddlerMessage>>()
        data.forEach {
            val copy = filter.messageFilter(it.items)
            if (copy.isNotEmpty())
                map[it.key] = copy
        }
        return map
    }
}

private data class MappingEntry(var time: Long, val key: String, val items: MutableList<ParsedNiddlerMessage>)