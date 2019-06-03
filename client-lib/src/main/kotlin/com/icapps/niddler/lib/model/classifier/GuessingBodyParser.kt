package com.icapps.niddler.lib.model.classifier

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
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
    }

    fun determineBodyType(): ConcreteBody? {
        if (bodyBytes == null || bodyBytes.isEmpty())
            return ConcreteBody(initialBodyFormat.format.type, initialBodyFormat.format.rawMimeType, data = null)

        if (initialBodyFormat.format.type == BodyFormatType.FORMAT_BINARY || initialBodyFormat.format.type == BodyFormatType.FORMAT_EMPTY)
            return determineBodyFromContent(bodyBytes) ?: ConcreteBody(initialBodyFormat.format.type,
                    initialBodyFormat.format.rawMimeType,
                    data = bodyBytes)

        initialBodyFormat.bodyParser.parse(initialBodyFormat.format, bodyBytes)?.let { return ConcreteBody(initialBodyFormat.format.type, initialBodyFormat.format.rawMimeType, it) }
        return null //Failed to parse
    }

    private fun determineBodyFromContent(content: ByteArray): ConcreteBody? {
        when (findFirstNonBlankByte(content)) {
            '{'.toByte(), '['.toByte() -> {
                val jsonParser = JsonBodyParser()
                val data = jsonParser.parse(BodyFormat(BodyFormatType.FORMAT_JSON, null, null), content) ?: return null
                return ConcreteBody(BodyFormatType.FORMAT_JSON, BodyFormatType.FORMAT_JSON.verbose, data)
            }
            '<'.toByte() -> {
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
        return null
    }

    private fun findFirstNonBlankByte(bytes: ByteArray): Byte? {
        val index = bytes.indexOfFirst { it != SPACE || it != CR || it != LF || it != TAB }
        return bytes.getOrNull(index)
    }

    private fun firstBytesContainHtml(bytes: ByteArray): Boolean {
        return String(bytes, 0, Math.min(bytes.size, 32)).contains("html", ignoreCase = true)
    }

}

data class ConcreteBody(
        val type: BodyFormatType,
        val rawType: String?,
        val data: Any?
)