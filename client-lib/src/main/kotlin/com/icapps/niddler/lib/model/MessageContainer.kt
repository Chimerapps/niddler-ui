package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class MessageContainer(private var bodyParser: NiddlerMessageBodyParser) : NiddlerMessageListener {

    private val knownMessageIds: MutableSet<String> = hashSetOf()

    private var currentFilter: String = ""

    fun clear() {
        synchronized(knownMessageIds) {
            knownMessageIds.clear()
            messagesByMessageRequestId.clear()
        }
    }



    fun getMessagesChronological(): List<ParsedNiddlerMessage> {
        val filter = currentFilter.trim()
        val doFilter = filter.isNotEmpty()

        val sortedMessages = ArrayList<ParsedNiddlerMessage>(knownMessageIds.size)
        synchronized(knownMessageIds) {
            if (doFilter)
                messagesByMessageRequestId.forEach { it -> if (filterAccept(filter, it.value)) sortedMessages.addAll(it.value) }
            else
                messagesByMessageRequestId.forEach { it -> sortedMessages.addAll(it.value) }
        }
        sortedMessages.sortBy { it.timestamp }

        return sortedMessages
    }

    fun getMessagesLinked(): Map<String, List<ParsedNiddlerMessage>> {
        val chronological = getMessagesChronological()
        val map: MutableMap<String, MutableList<ParsedNiddlerMessage>> = LinkedHashMap()
        chronological.forEach {
            var items = map[it.requestId]
            if (items == null) {
                items = arrayListOf(it)
                map[it.requestId] = items
            } else {
                items.add(it)
            }
        }
        return map
    }


    fun filtered(currentFilter: String): MessageContainer {
        this.currentFilter = currentFilter
        return this
    }

    private fun filterAccept(filter: String, messages: List<ParsedNiddlerMessage>): Boolean {
        return messages.firstOrNull {
            it.url?.contains(filter, ignoreCase = true) == true
        } != null
    }

}

interface ParsedNiddlerMessageListener {

    fun onMessage(message: ParsedNiddlerMessage)

}