package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage

/**
 * @author Nicola Verbeeck
 */
class ParsedNiddlerMessage(val message: NiddlerMessage,
                           val bodyFormat: BodyFormat,
                           val bodyData: Any?,
                           val parsedNetworkRequest: ParsedNiddlerMessage?,
                           val parsedNetworkReply: ParsedNiddlerMessage?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ParsedNiddlerMessage

        if (message.messageId != other.message.messageId) return false

        return true
    }

    override fun hashCode(): Int {
        return message.messageId.hashCode()
    }

}