package com.icapps.niddler.ui.model

import com.google.gson.JsonParser
import com.icapps.niddler.ui.util.logger
import org.apache.http.entity.ContentType
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URLDecoder
import java.nio.charset.Charset
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerMessageBodyParser {

    companion object {
        private val SPACE = 32.toByte()
        private val LF = 10.toByte()
        private val CR = 13.toByte()
        private val TAB = 9.toByte()

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

    fun parseBodyWithType(message: NiddlerMessage, contentType: BodyFormat): ParsedNiddlerMessage? {
        when (contentType.type) {
            BodyFormatType.FORMAT_JSON -> return examineJson(message.getBodyAsBytes, message)
            BodyFormatType.FORMAT_XML -> return examineXML(message.getBodyAsBytes, message, contentType)
            BodyFormatType.FORMAT_HTML, BodyFormatType.FORMAT_PLAIN -> {
                val bytes = message.getBodyAsBytes ?: return ParsedNiddlerMessage(contentType, null, message, parseBody(message.networkRequest), parseBody(message.networkReply))
                return ParsedNiddlerMessage(contentType, String(bytes, 0, bytes.size, Charset.forName(contentType.encoding ?: "UTF-8")), message, parseBody(message.networkRequest), parseBody(message.networkReply))
            }
            BodyFormatType.FORMAT_IMAGE -> return ParsedNiddlerMessage(contentType, ImageIO.read(ByteArrayInputStream(message.getBodyAsBytes)), message, parseBody(message.networkRequest), parseBody(message.networkReply))
            BodyFormatType.FORMAT_BINARY -> return ParsedNiddlerMessage(contentType, message.getBodyAsBytes, message, parseBody(message.networkRequest), parseBody(message.networkReply))
            BodyFormatType.FORMAT_FORM_ENCODED -> return examineFormEncoded(message.getBodyAsBytes, message) ?: throw IllegalArgumentException("Message is not form encoded")
            BodyFormatType.FORMAT_EMPTY -> return ParsedNiddlerMessage(contentType, null, message, parseBody(message.networkRequest), parseBody(message.networkReply))
        }
    }

    private fun parseMessage(message: NiddlerMessage): ParsedNiddlerMessage {
        val contentType = classifyFormatFromHeaders(message)
        if (contentType != null) {
            return parseBodyWithType(message, contentType) ?: return parseBodyWithType(message, BodyFormat.NONE)!!
        }
        return determineTypeFromBody(message)
    }

    private fun classifyFormatFromHeaders(message: NiddlerMessage): BodyFormat? {
        val contentTypeHeader = message.headers?.get("content-type")
        if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
            val contentTypeString = contentTypeHeader[0]
            val parsedContentType = ContentType.parse(contentTypeString)
            val fromMime = fromMime(parsedContentType.mimeType) ?: return null
            return BodyFormat(fromMime, parsedContentType.mimeType, parsedContentType.charset?.name())
        }
        return null
    }

    private fun determineTypeFromBody(message: NiddlerMessage): ParsedNiddlerMessage {
        val bodyAsBytes = message.getBodyAsBytes
        if (bodyAsBytes == null || bodyAsBytes.isEmpty())
            return parseBodyWithType(message, BodyFormat.NONE)!!

        val firstReasonableTextByte = findFirstNonBlankByte(bodyAsBytes)
        val parsed = when (firstReasonableTextByte) {
            '{'.toByte(), '['.toByte() -> examineJson(bodyAsBytes, message)
            '<'.toByte() ->
                examineXML(bodyAsBytes, message) ?:
                        if (firstBytesContainHtml(bodyAsBytes, "html"))
                            ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_HTML, null, null), String(bodyAsBytes, 0, bodyAsBytes.size), message, parseBody(message.networkRequest), parseBody(message.networkReply))
                        else
                            null
            else -> null
        }
        if (parsed != null)
            return parsed
        //TODO image
        return parseBodyWithType(message, BodyFormat.UNKNOWN)!!
    }

    private fun findFirstNonBlankByte(bytes: ByteArray): Byte? {
        bytes.forEach {
            if (!(it == SPACE || it == CR || it == LF || it == TAB)) {
                return it
            }
        }
        return null
    }

    private fun firstBytesContainHtml(bytes: ByteArray, string: String): Boolean {
        return String(bytes, 0, Math.min(bytes.size, 32)).contains(string, true)
    }

    private fun fromMime(mimeType: String): BodyFormatType? {
        when (mimeType.toLowerCase()) {
            "application/json" -> return BodyFormatType.FORMAT_JSON
            "application/xml", "text/xml", "application/dash+xml" -> return BodyFormatType.FORMAT_XML
            "application/octet-stream" -> return BodyFormatType.FORMAT_BINARY
            "text/html" -> return BodyFormatType.FORMAT_HTML
            "text/plain" -> return BodyFormatType.FORMAT_PLAIN
            "application/svg+xml" -> return BodyFormatType.FORMAT_XML //TODO this is an image...
            "application/x-www-form-urlencoded" -> return BodyFormatType.FORMAT_FORM_ENCODED
            "image/bmp", "image/png", "image/tiff", "image/jpg", "image/jpeg", "image/gif" -> return BodyFormatType.FORMAT_IMAGE
        }
        return null
    }

    private fun examineXML(bodyAsBytes: ByteArray?, message: NiddlerMessage, bodyType: BodyFormat? = null): ParsedNiddlerMessage? {
        if (bodyAsBytes == null || bodyAsBytes.isEmpty()) return null
        try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(bodyAsBytes))
            if (bodyType != null) {
                //TODO handle svg?
                return ParsedNiddlerMessage(bodyType, document, message, parseBody(message.networkRequest), parseBody(message.networkReply))
            }
            when (document.documentElement.tagName) {
                "svg" -> return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_XML, "application/svg+xml", document.inputEncoding), document, message, parseBody(message.networkRequest), parseBody(message.networkReply))
            }
            return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_XML, null, document.inputEncoding), document, message, parseBody(message.networkRequest), parseBody(message.networkReply))
        } catch (e: Exception) {
            return null
        }
    }

    private fun examineJson(bodyAsBytes: ByteArray?, message: NiddlerMessage): ParsedNiddlerMessage? {
        if (bodyAsBytes == null || bodyAsBytes.isEmpty()) return null
        try {
            val json = JsonParser().parse(InputStreamReader(ByteArrayInputStream(bodyAsBytes), Charsets.UTF_8))
            return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_JSON, null, Charsets.UTF_8.name()), json, message, parseBody(message.networkRequest), parseBody(message.networkReply))
        } catch (e: Exception) {
            return null
        }
    }

    private fun examineFormEncoded(bodyAsBytes: ByteArray?, message: NiddlerMessage): ParsedNiddlerMessage? {
        if (bodyAsBytes == null || bodyAsBytes.isEmpty()) return null

        val map: MutableMap<String, String> = mutableMapOf()
        String(bodyAsBytes).split('&').forEach {
            val parts = it.split('=')
            val key = URLDecoder.decode(parts[0], "UTF-8")
            val value = URLDecoder.decode(parts[1], "UTF-8")
            map[key] = value
        }
        return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_FORM_ENCODED, null, null), map, message, parseBody(message.networkRequest), parseBody(message.networkReply))
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

enum class BodyFormatType(val verbose: String) {
    FORMAT_JSON("application/json"),
    FORMAT_XML("application/xml"),
    FORMAT_PLAIN("text/plain"),
    FORMAT_IMAGE("image"),
    FORMAT_BINARY("binary"),
    FORMAT_HTML("text/html"),
    FORMAT_EMPTY(""),
    FORMAT_FORM_ENCODED("x-www-form-urlencoded")
}