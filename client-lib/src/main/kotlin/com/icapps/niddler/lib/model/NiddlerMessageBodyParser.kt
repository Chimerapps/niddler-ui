package com.icapps.niddler.lib.model

import com.chimerapps.discovery.utils.error
import com.chimerapps.discovery.utils.logger
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.classifier.BodyClassifier
import com.icapps.niddler.lib.model.classifier.ConcreteBody
import com.icapps.niddler.lib.model.classifier.GuessingBodyParser

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
                    message = message)
        }
    }

    private fun parseBodyWithType(message: NiddlerMessage, content: ConcreteBody?): ParsedNiddlerMessage {
        return ParsedNiddlerMessage(message, asFormat(content), content?.data)
    }

    private fun parseMessage(message: NiddlerMessage): ParsedNiddlerMessage {
        val contentType = classifyFormatFromHeaders(message)
        if (contentType != null) {
            return parseBodyWithType(message, contentType)
        }
        if (message.body.isNullOrEmpty()) {
            return ParsedNiddlerMessage(message, BodyFormat.NONE, null)
        }
        return ParsedNiddlerMessage(message, BodyFormat.UNKNOWN, message.getBodyAsBytes)
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