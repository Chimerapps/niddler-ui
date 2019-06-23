package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.utils.ObservableMutableList

class ObservableLinkedMessageList<T : NiddlerMessage>(private val storage: NiddlerMessageStorage<T>) {

    private val internalList = ObservableMutableList<LinkedMessageHolder<T>>(mutableListOf())

    fun addMessage(message: T): Boolean {
        synchronized(internalList) {
            val messageRequestId = message.requestId
            val existing = internalList.lastOrNull { it.requestId == messageRequestId }
            if (existing != null) {
                var requiresSort = false
                if (message.isRequest) {
                    existing.request = message
                    requiresSort = true
                } else {
                    if (message.timestamp < existing.time) {
                        existing.time = message.timestamp
                        requiresSort = false
                    }
                    existing.addResponse(message)
                }
                if (requiresSort)
                    internalList.sortBy { it.time }
            } else {
                val newEntry = LinkedMessageHolder(messageRequestId, message.timestamp, if (message.isRequest) message else null)
                if (!message.isRequest)
                    newEntry.responses.add(message)

                //Find insert position
                if (internalList.isEmpty()) {
                    internalList += newEntry
                } else {
                    val time = message.timestamp
                    if (internalList.last().time < time) {
                        internalList += newEntry
                    } else {
                        val index = internalList.indexOfLast { it.time > time }
                        if (index == -1) //Can't find any, this should not be the case anyway
                            internalList.add(newEntry)
                        else
                            internalList.add(index, newEntry)
                    }
                }
            }
            Unit //Or else some ifs are sad
        }
        return true
    }

}

private data class LinkedMessageHolder<T : NiddlerMessage>(val requestId: String,
                                                           var time: Long,
                                                           var request: T?,
                                                           var responses: MutableList<T> = mutableListOf()) {

    fun addResponse(message: T) {
        if (responses.isEmpty()) {
            responses.add(message)
        } else {
            val time = message.timestamp
            if (responses.last().timestamp < time) {
                responses.add(message)
            } else {
                val index = responses.indexOfLast { it.timestamp > time }
                if (index == -1) //Can't find any, this should not be the case anyway
                    responses.add(message)
                else
                    responses.add(index, message)
            }
        }
    }

}