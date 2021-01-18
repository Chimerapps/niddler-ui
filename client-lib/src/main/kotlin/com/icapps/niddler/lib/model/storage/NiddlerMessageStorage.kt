package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.NiddlerMessageInfo

/**
 * @author Nicola Verbeeck
 */
interface NiddlerMessageStorage {

    fun addMessage(message: NiddlerMessage)

    fun allMessages(): List<NiddlerMessage>

    /**
     * If more performant, implementations can skip loading nested network requests/responses
     */
    fun loadMessage(message: NiddlerMessageInfo): NiddlerMessage?

    fun loadMessageHeaders(message: NiddlerMessageInfo): Map<String, List<String>>?

    fun loadMessageMetadata(message: NiddlerMessageInfo): Map<String, String>?

    fun clear()

    fun isEmpty(): Boolean

    interface Filter {

        fun messageFilter(message: NiddlerMessageInfo, storage: NiddlerMessageContainer): Boolean

        fun messageFilter(relatedMessages: List<NiddlerMessageInfo>): List<NiddlerMessageInfo>
    }

}