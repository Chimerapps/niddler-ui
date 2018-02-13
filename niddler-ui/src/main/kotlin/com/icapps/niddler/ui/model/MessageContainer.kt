package com.icapps.niddler.ui.model

import com.icapps.niddler.ui.connection.NiddlerMessageListener
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class MessageContainer(private var bodyParser: NiddlerMessageBodyParser) : NiddlerMessageListener {

    private val knownMessageIds: MutableSet<String> = hashSetOf()
    private val messagesByMessageRequestId: MutableMap<String, MutableList<ParsedNiddlerMessage>> = hashMapOf()

    private val listeners: MutableSet<ParsedNiddlerMessageListener> = hashSetOf()
    private var currentFilter: String = ""

    fun clear() {
        synchronized(knownMessageIds) {
            knownMessageIds.clear()
            messagesByMessageRequestId.clear()
        }
    }

    fun addMessage(msg: ParsedNiddlerMessage) {
        synchronized(knownMessageIds) {
            if (knownMessageIds.add(msg.messageId)) {
                var list = messagesByMessageRequestId[msg.requestId]
                if (list == null) {
                    list = arrayListOf(msg)
                    messagesByMessageRequestId[msg.requestId] = list
                } else {
                    list.add(msg)
                    list.sortBy { it.timestamp }
                }
            }
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

    override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
        val parsedMessage = bodyParser.parseBody(niddlerMessage) ?: return
        addMessage(parsedMessage)
        synchronized(listeners) {
            listeners.forEach { it.onMessage(parsedMessage) }
        }
    }

    fun registerListener(listener: ParsedNiddlerMessageListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: ParsedNiddlerMessageListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun getMessagesWithRequestId(requestId: String): List<ParsedNiddlerMessage>? {
        return synchronized(knownMessageIds) { messagesByMessageRequestId[requestId] }
    }

    fun findResponse(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return getMessagesWithRequestId(message.requestId)?.find {
            !it.isRequest
        }
    }

    fun findRequest(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return getMessagesWithRequestId(message.requestId)?.find(ParsedNiddlerMessage::isRequest)
    }

    override fun onServerInfo(serverInfo: NiddlerServerInfo) {
        //Ignore
    }

    override fun onAuthRequest(): String? {
        //We don't know anything about authentication
        return null
    }

    override fun onReady() {
        //Ignore
    }

    override fun onClosed() {
        //Ignore
    }

    override fun onDebuggerAttached() {
        //Ignore
    }

    override fun onDebuggerActive() {
        //Ignore
    }

    override fun onDebuggerInactive() {
        //Ignore
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