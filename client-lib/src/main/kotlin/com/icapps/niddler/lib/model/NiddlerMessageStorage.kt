package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage

/**
 * @author Nicola Verbeeck
 */
interface NiddlerMessageStorage<T : NiddlerMessage> {

    val messagesChronological: ObservableChronologicalMessageList<T>
    val messagesLinked: ObservableLinkedMessageList<T>

    fun addMessage(message: T)

    fun getMessagesWithRequestId(requestId: String): List<T>

    fun findResponse(message: T): T?

    fun findRequest(message: T): T?

    fun clear()

    fun isEmpty(): Boolean

    interface Filter<T : NiddlerMessage> {

        fun messageFilter(message: T, storage: NiddlerMessageStorage<T>): Boolean

        fun messageFilter(relatedMessages: List<T>): List<T>
    }

}

class InMemoryNiddlerMessageStorage<T : NiddlerMessage> : NiddlerMessageStorage<T> {

    override val messagesChronological: ObservableChronologicalMessageList<T> = ObservableChronologicalMessageList(this)
    override val messagesLinked = ObservableLinkedMessageList(this)

    override fun clear() {
        synchronized(this) {
            messagesChronological.clear()
            messagesLinked.clear()
        }
    }

    override fun addMessage(message: T) {
        synchronized(this) {
            if (messagesChronological.addMessage(message))
                messagesLinked.addMessage(message)
        }
    }

    override fun getMessagesWithRequestId(requestId: String): List<T> {
        return synchronized(this) {
            messagesLinked[requestId] ?: emptyList()
        }
    }

    override fun findResponse(message: T): T? {
        return getMessagesWithRequestId(message.requestId).find {
            !it.isRequest
        }
    }

    override fun findRequest(message: T): T? {
        return getMessagesWithRequestId(message.requestId).findLast(NiddlerMessage::isRequest)
    }

    override fun isEmpty(): Boolean {
        return messagesChronological.isEmpty()
    }
}
