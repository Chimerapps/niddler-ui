package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.ObservableChronologicalMessageList
import com.icapps.niddler.lib.model.ObservableLinkedMessageList

class InMemoryNiddlerMessageStorage : NiddlerMessageStorage {

    private val internalList = arrayListOf<NiddlerMessage>()

    override fun clear() {
        synchronized(this) {
            internalList.clear()
        }
    }

    override fun addMessage(message: NiddlerMessage) {
        synchronized(this) {
            internalList += message
        }
    }

    override fun allMessages(): List<NiddlerMessage> {
        synchronized(this) {
            return ArrayList(internalList)
        }
    }

    override fun isEmpty(): Boolean {
        return internalList.isEmpty()
    }
}