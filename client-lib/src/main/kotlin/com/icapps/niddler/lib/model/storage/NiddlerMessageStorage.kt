package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.ObservableChronologicalMessageList
import com.icapps.niddler.lib.model.ObservableLinkedMessageList

/**
 * @author Nicola Verbeeck
 */
interface NiddlerMessageStorage {

    val messagesChronological: ObservableChronologicalMessageList
    val messagesLinked: ObservableLinkedMessageList

    fun addMessage(message: NiddlerMessage)

    fun getMessagesWithRequestId(requestId: String): List<NiddlerMessage>

    fun findResponse(message: NiddlerMessage): NiddlerMessage?

    fun findRequest(message: NiddlerMessage): NiddlerMessage?

    fun findRequest(requestId: String): NiddlerMessage?

    fun clear()

    fun isEmpty(): Boolean

    interface Filter {

        fun messageFilter(message: NiddlerMessage, storage: NiddlerMessageStorage): Boolean

        fun messageFilter(relatedMessages: List<NiddlerMessage>): List<NiddlerMessage>
    }

}