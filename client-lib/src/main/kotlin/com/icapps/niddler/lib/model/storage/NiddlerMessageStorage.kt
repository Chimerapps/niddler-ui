package com.icapps.niddler.lib.model.storage

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.NiddlerMessageInfo

/**
 * @author Nicola Verbeeck
 */
interface NiddlerMessageStorage {

    fun addMessage(message: NiddlerMessage)

    fun allMessages() : List<NiddlerMessage>

    fun clear()

    fun isEmpty(): Boolean

    interface Filter {

        fun messageFilter(message: NiddlerMessageInfo, storage: NiddlerMessageContainer): Boolean

        fun messageFilter(relatedMessages: List<NiddlerMessageInfo>): List<NiddlerMessageInfo>
    }

}