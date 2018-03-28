package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage

/**
 * @author nicolaverbeeck
 */
class SimpleUrlMatchFilter<T : NiddlerMessage>(toMatch: String) : NiddlerMessageStorage.Filter<T> {

    private val toMatch = toMatch.trim()

    override fun messageFilter(message: T, storage: NiddlerMessageStorage<T>): Boolean {
        if (toMatch.isEmpty())
            return true

        val url = if (message.isRequest)
            message.url
        else
            storage.findRequest(message)?.url
        return url?.contains(toMatch, ignoreCase = true) == true
    }

    override fun messageFilter(relatedMessages: List<T>): List<T> {
        if (toMatch.isEmpty())
            return ArrayList(relatedMessages)

        if (relatedMessages.firstOrNull { it.url?.contains(toMatch, ignoreCase = true) == true } == null)
            return emptyList()

        return ArrayList(relatedMessages)
    }
}