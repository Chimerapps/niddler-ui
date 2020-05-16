package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageInfo

@Deprecated(message = "Uses a lot of memory without optimizations",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith(expression = "QuickBinaryMessageStorage",
                imports = ["com.icapps.niddler.lib.model.storage.binary.QuickBinaryMessageStorage"]))
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
            message.networkRequest?.let(internalList::add)
            message.networkReply?.let(internalList::add)
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

    override fun loadMessage(message: NiddlerMessageInfo): NiddlerMessage? {
        synchronized(this) {
            return internalList.find { it.messageId == message.messageId }
        }
    }

    override fun loadMessageHeaders(message: NiddlerMessageInfo): Map<String, List<String>>? {
        synchronized(this) {
            return internalList.find { it.messageId == message.messageId }?.headers
        }
    }
}