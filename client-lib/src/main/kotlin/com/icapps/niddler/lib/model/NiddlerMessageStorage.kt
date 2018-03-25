package com.icapps.niddler.lib.model

/**
 * @author nicolaverbeeck
 */
interface NiddlerMessageStorage {

    val messagesChronological: List<ParsedNiddlerMessage>
    val messagesLinked: Map<String, List<ParsedNiddlerMessage>>
    var filter: Filter?

    fun addMessage(message: ParsedNiddlerMessage)

    fun getMessagesWithRequestId(requestId: String): List<ParsedNiddlerMessage>

    fun findResponse(message: ParsedNiddlerMessage): ParsedNiddlerMessage?

    fun findRequest(message: ParsedNiddlerMessage): ParsedNiddlerMessage?

    fun clear()

    interface Filter {
        fun messageFilter(message: ParsedNiddlerMessage, storage: NiddlerMessageStorage): Boolean
    }

}

class InMemoryNiddlerMessageStorage : NiddlerMessageStorage {

    private val messagesList: MutableList<ParsedNiddlerMessage> = arrayListOf()
    private val messagesMapped: MutableMap<String, MutableList<ParsedNiddlerMessage>> = hashMapOf()

    override val messagesChronological: List<ParsedNiddlerMessage>
        get() = synchronized(messagesList) { ArrayList(messagesList) }

    override val messagesLinked: Map<String, List<ParsedNiddlerMessage>>
        get() = synchronized(messagesList) {
            messagesList.groupBy { it.requestId }
        }

    override var filter: NiddlerMessageStorage.Filter? = null

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
            messagesMapped.getOrPut(message.messageId, { arrayListOf() }).apply {
                add(message)
                sortBy { it.timestamp }
            }
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

}