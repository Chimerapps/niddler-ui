package com.icapps.niddler.lib.model

/**
 * @author nicolaverbeeck
 */
class SimpleUrlMatchFilter(toMatch: String) : NiddlerMessageStorage.Filter {

    private val toMatch = toMatch.trim()

    override fun messageFilter(message: ParsedNiddlerMessage, storage: NiddlerMessageStorage): Boolean {
        if (toMatch.isEmpty())
            return true

        val url = if (message.isRequest)
            message.url
        else
            storage.findRequest(message)?.url
        return url?.contains(toMatch, ignoreCase = true) == true
    }

    override fun messageFilter(relatedMessages: List<ParsedNiddlerMessage>): List<ParsedNiddlerMessage> {
        if (toMatch.isEmpty())
            return ArrayList(relatedMessages)

        if (relatedMessages.firstOrNull { it.url?.contains(toMatch, ignoreCase = true) == true } == null)
            return emptyList()

        return ArrayList(relatedMessages)
    }
}