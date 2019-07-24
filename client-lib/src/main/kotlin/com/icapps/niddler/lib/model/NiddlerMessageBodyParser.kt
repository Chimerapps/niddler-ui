package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.classifier.BodyClassifier
import com.icapps.niddler.lib.model.classifier.ConcreteBody
import com.icapps.niddler.lib.model.classifier.GuessingBodyParser
import com.icapps.niddler.lib.utils.error
import com.icapps.niddler.lib.utils.logger

/**
 * @author Nicola Verbeeck
 */
class NiddlerMessageBodyParser(private val classifier: BodyClassifier) {

    companion object {
        private val log = logger<NiddlerMessageBodyParser>()
    }

    fun parseBody(message: NiddlerMessage): ParsedNiddlerMessage {
        return parseBodyInternal(message)!!
    }

    private fun parseBodyInternal(message: NiddlerMessage?): ParsedNiddlerMessage? {
        if (message == null)
            return null
        return try {
            parseMessage(message)
        } catch (e: Throwable) {
            log.error("Message parse failure: ", e)
            ParsedNiddlerMessage(
                    bodyFormat = BodyFormat(
                            type = BodyFormatType.FORMAT_BINARY,
                            rawMimeType = null,
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
        return GuessingBodyParser(classifier.classifyFormat(message), message.getBodyAsBytes).determineBodyType()
    }

    private fun asFormat(content: ConcreteBody?): BodyFormat {
        content?.let {
            return BodyFormat(it.type, it.rawType, null)
        }
        return BodyFormat.UNKNOWN
    }
}