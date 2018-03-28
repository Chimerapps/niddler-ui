package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.classifier.BodyFormatType
import com.icapps.niddler.lib.model.classifier.BodyParser
import com.icapps.niddler.lib.model.classifier.ConcreteBody
import com.icapps.niddler.lib.model.classifier.SimpleBodyClassifier
import com.icapps.niddler.lib.utils.error
import com.icapps.niddler.lib.utils.logger

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerMessageBodyParser(private val classifier: SimpleBodyClassifier) {

    companion object {
        private val log = logger<NiddlerMessageBodyParser>()
    }

    fun parseBody(message: NiddlerMessage): ParsedNiddlerMessage {
        return parseBodyInternal(message)!!
    }

    private fun parseBodyInternal(message: NiddlerMessage?): ParsedNiddlerMessage? {
        if (message == null)
            return null
        try {
            return parseMessage(message)
        } catch (e: Throwable) {
            log.error("Message parse failure: ", e)
            return ParsedNiddlerMessage(
                    bodyFormat = BodyFormat(
                            type = BodyFormatType.FORMAT_BINARY,
                            subtype = null,
                            encoding = null),
                    bodyData = message.getBodyAsBytes,
                    message = message,
                    parsedNetworkRequest = parseBodyInternal(message.networkRequest),
                    parsedNetworkReply = parseBodyInternal(message.networkReply))
        }
    }

    private fun parseBodyWithType(message: NiddlerMessage, content: ConcreteBody?): ParsedNiddlerMessage {
        return ParsedNiddlerMessage(message, asFormat(content), content?.data,
                parseBodyInternal(message.networkRequest),
                parseBodyInternal(message.networkReply))
    }

    private fun parseMessage(message: NiddlerMessage): ParsedNiddlerMessage {
        val contentType = classifyFormatFromHeaders(message)
        if (contentType != null) {
            return parseBodyWithType(message, contentType)
        }
        if (message.body.isNullOrEmpty()) {
            return ParsedNiddlerMessage(message, BodyFormat.NONE, null,
                    parseBodyInternal(message.networkRequest), parseBodyInternal(message.networkReply))
        }
        return ParsedNiddlerMessage(message, BodyFormat.UNKNOWN, message.getBodyAsBytes,
                parseBodyInternal(message.networkRequest),
                parseBodyInternal(message.networkReply))
    }

    private fun classifyFormatFromHeaders(message: NiddlerMessage): ConcreteBody? {
        return BodyParser(classifier.classifyFormat(message), message.getBodyAsBytes).determineBodyType()
    }

    private fun asFormat(content: ConcreteBody?): BodyFormat {
        content?.let {
            return BodyFormat(it.type, it.rawType, null)
        }
        return BodyFormat.UNKNOWN
    }
}

data class BodyFormat(val type: BodyFormatType, val subtype: String?, val encoding: String?) {
    companion object {
        val NONE = BodyFormat(BodyFormatType.FORMAT_EMPTY, null, null)
        val UNKNOWN = BodyFormat(BodyFormatType.FORMAT_BINARY, null, null)
    }

    override fun toString(): String {
        return type.verbose
    }
}
