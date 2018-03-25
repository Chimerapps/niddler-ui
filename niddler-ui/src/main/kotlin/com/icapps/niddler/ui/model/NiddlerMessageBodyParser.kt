package com.icapps.niddler.ui.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.ui.util.BodyFormatType
import com.icapps.niddler.ui.util.BodyTools
import com.icapps.niddler.ui.util.ConcreteBody
import com.icapps.niddler.ui.util.logger
import org.apache.http.entity.ContentType

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerMessageBodyParser {

    companion object {
        private val log = logger<NiddlerMessageBodyParser>()
    }

    fun parseBody(message: NiddlerMessage?): ParsedNiddlerMessage? {
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
                    parsedNetworkRequest = parseBody(message.networkRequest),
                    parsedNetworkReply = parseBody(message.networkReply))
        }
    }

    fun parseBodyWithType(message: NiddlerMessage, content: ConcreteBody?): ParsedNiddlerMessage {
        return ParsedNiddlerMessage(asFormat(content), content?.data, message,
                parseBody(message.networkRequest),
                parseBody(message.networkReply))
    }

    private fun parseMessage(message: NiddlerMessage): ParsedNiddlerMessage {
        val contentType = classifyFormatFromHeaders(message)
        if (contentType != null) {
            return parseBodyWithType(message, contentType)
        }
        return ParsedNiddlerMessage(BodyFormat.UNKNOWN, message.getBodyAsBytes, message,
                parseBody(message.networkRequest),
                parseBody(message.networkReply))
    }

    private fun classifyFormatFromHeaders(message: NiddlerMessage): ConcreteBody? {
        val contentTypeHeader = message.headers?.get("content-type")
        if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
            val contentTypeString = contentTypeHeader[0]
            val parsedContentType = ContentType.parse(contentTypeString)
            return BodyTools(parsedContentType.mimeType, message.getBodyAsBytes).determineBodyType()
        }
        return null
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
