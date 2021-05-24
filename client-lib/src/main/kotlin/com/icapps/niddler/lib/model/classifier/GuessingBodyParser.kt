package com.icapps.niddler.lib.model.classifier

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.bodyparser.ImageBodyParser
import com.icapps.niddler.lib.model.bodyparser.JsonBodyParser
import com.icapps.niddler.lib.model.bodyparser.XmlBodyParser

/**
 * @author Nicola Verbeeck
 */
class GuessingBodyParser(private val initialBodyFormat: BodyClassifierResult, private val bodyBytes: ByteArray?) {

    private companion object {
        private const val SPACE = 32.toByte()
        private const val LF = 10.toByte()
        private const val CR = 13.toByte()
        private const val TAB = 9.toByte()
        private const val MAX_ASCII_CHECK_LENGTH = 80*1024

        private val MAGIC_NUMBER_BMP = byteArrayOf(0x42.toByte(), 0x4D.toByte())
        private val MAGIC_NUMBER_PNG = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())
        private val MAGIC_NUMBER_GIF = byteArrayOf(0x47.toByte(), 0x49.toByte(), 0x46.toByte(), 0x38.toByte())
        private val MAGIC_NUMBER_JPG = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
        private val MAGIC_NUMBER_WEBP = byteArrayOf(0x52.toByte(), 0x49.toByte())
    }

    fun determineBodyType(): ConcreteBody? {
        if (bodyBytes == null || bodyBytes.isEmpty())
            return ConcreteBody(initialBodyFormat.format.type, initialBodyFormat.format.rawMimeType, data = null)

        if (initialBodyFormat.format.type == BodyFormatType.FORMAT_BINARY
                || initialBodyFormat.format.type == BodyFormatType.FORMAT_EMPTY
                || initialBodyFormat.format.type == BodyFormatType.FORMAT_PLAIN)
            return determineBodyFromContent(bodyBytes) ?: ConcreteBody(initialBodyFormat.format.type,
                    initialBodyFormat.format.rawMimeType,
                    data = bodyBytes)

        initialBodyFormat.bodyParser.parse(initialBodyFormat.format, bodyBytes)?.let { return ConcreteBody(initialBodyFormat.format.type, initialBodyFormat.format.rawMimeType, it) }
        //Failed to parse. Quick check to see if this is a binary or not
        if (bodyBytes.size <= MAX_ASCII_CHECK_LENGTH) {
            if (bodyBytes.all { it in 32..126 || it == 10.toByte() || it == 13.toByte() }) {
                //Seems like text!
                return ConcreteBody(BodyFormatType.FORMAT_PLAIN,
                        initialBodyFormat.format.rawMimeType,
                        data = bodyBytes)
            }
        }
        //When all else fails, binary...
        return ConcreteBody(BodyFormatType.FORMAT_BINARY,
                initialBodyFormat.format.rawMimeType,
                data = bodyBytes)
    }

    private fun determineBodyFromContent(content: ByteArray): ConcreteBody? {
        when (findFirstNonBlankByte(content)) {
            '{'.code, '['.code -> {
                val jsonParser = JsonBodyParser()
                val data = jsonParser.parse(BodyFormat(BodyFormatType.FORMAT_JSON, null, null), content) ?: return null
                return ConcreteBody(BodyFormatType.FORMAT_JSON, BodyFormatType.FORMAT_JSON.verbose, data)
            }
            '<'.code -> {
                val xmlParser = XmlBodyParser()
                val data = xmlParser.parse(BodyFormat(BodyFormatType.FORMAT_XML, null, null), content)
                if (data != null) {
                    return if (firstBytesContainHtml(content))
                        ConcreteBody(BodyFormatType.FORMAT_HTML, BodyFormatType.FORMAT_HTML.verbose, data)
                    else
                        ConcreteBody(BodyFormatType.FORMAT_XML, BodyFormatType.FORMAT_XML.verbose, data) //TODO image svg
                }
            }
        }
        if (checkMagic(MAGIC_NUMBER_BMP, content)) return checkImage(content, "image/bmp")
        if (checkMagic(MAGIC_NUMBER_PNG, content)) return checkImage(content, "image/png")
        if (checkMagic(MAGIC_NUMBER_JPG, content)) return checkImage(content, "image/jpeg")
        if (checkMagic(MAGIC_NUMBER_GIF, content)) return checkImage(content, "image/gif")
        if (checkMagic(MAGIC_NUMBER_WEBP, content)) return checkImage(content, "image/webp")

        if (content.size <= MAX_ASCII_CHECK_LENGTH) {
            if (content.all { it in 32..126 || it == 10.toByte() || it == 13.toByte() }) {
                //Seems like text!
                return ConcreteBody(BodyFormatType.FORMAT_PLAIN,
                        initialBodyFormat.format.rawMimeType,
                        data = bodyBytes)
            }
        }

        return null
    }

    private fun findFirstNonBlankByte(bytes: ByteArray): Int? {
        val index = bytes.indexOfFirst { it != SPACE || it != CR || it != LF || it != TAB }
        return bytes.getOrNull(index)?.toInt()
    }

    private fun firstBytesContainHtml(bytes: ByteArray): Boolean {
        return String(bytes, 0, bytes.size.coerceAtMost(32)).contains("html", ignoreCase = true)
    }

    private fun checkMagic(toCheck: ByteArray, content: ByteArray): Boolean {
        if (content.size < toCheck.size) return false
        for (i in toCheck.indices) {
            if (toCheck[i] != content[i]) return false
        }
        return true
    }

    private fun checkImage(content: ByteArray, mime: String): ConcreteBody? {
        try {
            ImageBodyParser().parse(BodyFormat(BodyFormatType.FORMAT_IMAGE, mime, null), content)?.let {
                return ConcreteBody(BodyFormatType.FORMAT_IMAGE, mime, it)
            }
        } catch (e: Throwable) {
        }
        return null
    }
}

data class ConcreteBody(
        val type: BodyFormatType,
        val rawType: String?,
        val data: Any?
)