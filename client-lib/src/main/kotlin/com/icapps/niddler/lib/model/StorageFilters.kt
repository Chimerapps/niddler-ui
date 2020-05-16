package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage

/**
 * @author Nicola Verbeeck
 */
class SimpleUrlMatchFilter(toMatch: String) : NiddlerMessageStorage.Filter {

    private val toMatch = toMatch.trim()

    override fun messageFilter(message: NiddlerMessageInfo, storage: NiddlerMessageContainer): Boolean {
        if (toMatch.isEmpty())
            return true

        val url = if (message.isRequest)
            message.url
        else
            storage.findRequest(message)?.url
        return url?.contains(toMatch, ignoreCase = true) == true
    }

    override fun messageFilter(relatedMessages: List<NiddlerMessageInfo>): List<NiddlerMessageInfo> {
        if (toMatch.isEmpty())
            return ArrayList(relatedMessages)

        if (relatedMessages.firstOrNull { it.url?.contains(toMatch, ignoreCase = true) == true } == null)
            return emptyList()

        return ArrayList(relatedMessages)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleUrlMatchFilter

        if (toMatch != other.toMatch) return false
        return true
    }

    override fun hashCode(): Int {
        return toMatch.hashCode()
    }


}