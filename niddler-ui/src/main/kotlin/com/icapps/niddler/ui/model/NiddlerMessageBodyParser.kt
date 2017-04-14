package com.icapps.niddler.ui.model

import com.google.gson.JsonParser
import org.apache.http.entity.ContentType
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URLDecoder
import java.nio.charset.Charset
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
    }

    fun parseBody(message: NiddlerMessage): ParsedNiddlerMessage {
        val format = parseMessage(message)
        return format
    }

    fun parseBodyWithType(message: NiddlerMessage, contentType: BodyFormat): ParsedNiddlerMessage {
        when (contentType.type) {
            BodyFormatType.FORMAT_JSON -> return examineJson(message.getBodyAsBytes, message) ?: throw IllegalArgumentException("Message is not json")
            BodyFormatType.FORMAT_XML -> return examineXML(message.getBodyAsBytes, message, contentType) ?: throw IllegalArgumentException("Message is not xml")
            BodyFormatType.FORMAT_PLAIN -> {
                val bytes = message.getBodyAsBytes ?: return ParsedNiddlerMessage(contentType, null, message)
                return ParsedNiddlerMessage(contentType, String(bytes, 0, bytes.size, Charset.forName(contentType.encoding ?: "UTF-8")), message)
            }
            BodyFormatType.FORMAT_IMAGE -> TODO("Images not yet supported, sorry!")
            BodyFormatType.FORMAT_BINARY -> return ParsedNiddlerMessage(contentType, message.getBodyAsBytes, message)
            BodyFormatType.FORMAT_FORM_ENCODED -> return examineFormEncoded(message.getBodyAsBytes, message) ?: throw IllegalArgumentException("Message is not form encoded")
            BodyFormatType.FORMAT_EMPTY -> return ParsedNiddlerMessage(contentType, null, message)
        }
    }

    private fun parseMessage(message: NiddlerMessage): ParsedNiddlerMessage {
        val contentType = classifyFormatFromHeaders(message)
        if (contentType != null) {
            return parseBodyWithType(message, contentType)
        }
        return determineTypeFromBody(message)
    }

    private fun classifyFormatFromHeaders(message: NiddlerMessage): BodyFormat? {
        val contentTypeHeader = message.headers?.get("Content-Type")
        if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
            val contentTypeString = contentTypeHeader[0]
            val parsedContentType = ContentType.parse(contentTypeString)
            return BodyFormat(fromMime(parsedContentType.mimeType), parsedContentType.mimeType, parsedContentType.charset?.name())
        }
        return null
    }

    private fun determineTypeFromBody(message: NiddlerMessage): ParsedNiddlerMessage {
        val bodyAsBytes = message.getBodyAsBytes
        if (bodyAsBytes == null || bodyAsBytes.isEmpty())
            return parseBodyWithType(message, BodyFormat.NONE)

        val firstReasonableTextByte = findFirstNonBlankByte(bodyAsBytes)
        val parsed = when (firstReasonableTextByte) {
            '{'.toByte(), '['.toByte() -> examineJson(bodyAsBytes, message)
            '<'.toByte() ->
                examineXML(bodyAsBytes, message) ?:
                        if (firstBytesContainHtml(bodyAsBytes, "html"))
                            ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_PLAIN, null, null), String(bodyAsBytes, 0, bodyAsBytes.size), message)
                        else
                            null
            else -> null
        }
        if (parsed != null)
            return parsed
        //TODO image
        return parseBodyWithType(message, BodyFormat.UNKNOWN)
    }

    private fun findFirstNonBlankByte(bytes: ByteArray): Byte? {
        bytes.forEach {
            if (it.toInt() <= SPACE) {
                if (!(it == SPACE || it == LF || it == CR)) {
                    return null
                }
            } else if (it.toInt() >= 127) {
                return null
            }
            return it
        }
        return null
    }

    private fun firstBytesContainHtml(bytes: ByteArray, string: String): Boolean {
        return String(bytes, 0, Math.min(bytes.size, 32)).contains(string, true)
    }

    private fun fromMime(mimeType: String): BodyFormatType {
        when (mimeType.toLowerCase()) {
            "application/json" -> return BodyFormatType.FORMAT_JSON
            "application/xml", "text/xml" -> return BodyFormatType.FORMAT_XML
            "application/octet-stream" -> return BodyFormatType.FORMAT_BINARY
            "text/html", "text/plain" -> return BodyFormatType.FORMAT_PLAIN
            "application/svg+xml" -> return BodyFormatType.FORMAT_IMAGE
            "application/x-www-form-urlencoded" -> return BodyFormatType.FORMAT_FORM_ENCODED
            "image/bmp", "image/png", "image/tiff" -> return BodyFormatType.FORMAT_IMAGE
        }
        return BodyFormatType.FORMAT_BINARY
    }

    private fun examineXML(bodyAsBytes: ByteArray?, message: NiddlerMessage, bodyType: BodyFormat? = null): ParsedNiddlerMessage? {
        if (bodyAsBytes == null) return null
        try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(bodyAsBytes))
            if (bodyType != null) {
                //TODO handle svg?
                return ParsedNiddlerMessage(bodyType, document, message)
            }
            when (document.documentElement.tagName) {
                "svg" -> return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_IMAGE, "application/svg+xml", document.inputEncoding), document, message)
            }
            return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_XML, null, document.inputEncoding), document, message)
        } catch(e: Exception) {
            return null
        }
    }

    private fun examineJson(bodyAsBytes: ByteArray?, message: NiddlerMessage): ParsedNiddlerMessage? {
        if (bodyAsBytes == null) return null
        try {
            val json = JsonParser().parse(InputStreamReader(ByteArrayInputStream(bodyAsBytes), Charsets.UTF_8))
            return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_JSON, null, Charsets.UTF_8.name()), json, message)
        } catch(e: Exception) {
            return null
        }
    }

    private fun examineFormEncoded(bodyAsBytes: ByteArray?, message: NiddlerMessage): ParsedNiddlerMessage? {
        if (bodyAsBytes == null) return null

        val map: MutableMap<String, String> = mutableMapOf()
        String(bodyAsBytes).split('&').forEach {
            val parts = it.split('=')
            val key = URLDecoder.decode(parts[0], "UTF-8")
            val value = URLDecoder.decode(parts[1], "UTF-8")
            map[key] = value
        }
        return ParsedNiddlerMessage(BodyFormat(BodyFormatType.FORMAT_FORM_ENCODED, null, null), map, message)
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
    FORMAT_EMPTY(""),
    FORMAT_FORM_ENCODED("x-www-form-urlencoded")
}