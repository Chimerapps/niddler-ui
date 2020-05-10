package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.ObservableChronologicalMessageList
import com.icapps.niddler.lib.model.ObservableLinkedMessageList

class InMemoryNiddlerMessageStorage : NiddlerMessageStorage {

    override val messagesChronological: ObservableChronologicalMessageList = ObservableChronologicalMessageList(this)
    override val messagesLinked = ObservableLinkedMessageList(this)

    override fun clear() {
        synchronized(this) {
            messagesChronological.clear()
            messagesLinked.clear()
        }
    }

    override fun addMessage(message: NiddlerMessage) {
        synchronized(this) {
            if (messagesChronological.addMessage(message))
                messagesLinked.addMessage(message)
        }
    }

    override fun getMessagesWithRequestId(requestId: String): List<NiddlerMessage> {
        return synchronized(this) {
            messagesLinked[requestId] ?: emptyList()
        }
    }

    override fun findResponse(message: NiddlerMessage): NiddlerMessage? {
        return getMessagesWithRequestId(message.requestId).find {
            !it.isRequest
        }
    }

    override fun findRequest(message: NiddlerMessage): NiddlerMessage? {
        return findRequest(message.requestId)
    }

    override fun findRequest(requestId: String): NiddlerMessage? {
        return getMessagesWithRequestId(requestId).findLast(NiddlerMessage::isRequest)
    }

    override fun isEmpty(): Boolean {
        return messagesChronological.isEmpty()
    }
}