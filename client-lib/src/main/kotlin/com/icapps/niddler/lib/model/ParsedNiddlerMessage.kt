package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class ParsedNiddlerMessage(val message: NiddlerMessage,
                           val bodyFormat: BodyFormat,
                           val bodyData: Any?,
                           val parsedNetworkRequest: ParsedNiddlerMessage?,
                           val parsedNetworkReply: ParsedNiddlerMessage?) : NiddlerMessage by message {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as NiddlerMessage

        if (messageId != other.messageId) return false

        return true
    }

    override fun hashCode(): Int {
        return messageId.hashCode()
    }

}