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

}