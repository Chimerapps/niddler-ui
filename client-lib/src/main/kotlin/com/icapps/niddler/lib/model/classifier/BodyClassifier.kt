package com.icapps.niddler.lib.model.classifier

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.BodyParser
import com.icapps.niddler.lib.model.bodyparser.BinaryBodyParser
import com.icapps.niddler.lib.model.bodyparser.HtmlBodyParser
import com.icapps.niddler.lib.model.bodyparser.ImageBodyParser
import com.icapps.niddler.lib.model.bodyparser.JsonBodyParser
import com.icapps.niddler.lib.model.bodyparser.PlainTextBodyParser
import com.icapps.niddler.lib.model.bodyparser.URLEncodedBodyParser
import com.icapps.niddler.lib.model.bodyparser.XmlBodyParser
import org.apache.http.entity.ContentType

/**
 * @author Nicola Verbeeck
 *
 * Classify the body format of the given message
 */
data class BodyClassifierResult(val format: BodyFormat, val bodyParser: BodyParser<*>)

interface BodyClassifier {

    fun classifyFormat(message: NiddlerMessage): BodyClassifierResult

}

interface BodyFormatClassifierExtension {

    fun classifyFormat(message: NiddlerMessage): BodyClassifierResult?

    fun classifyFormat(mimeType: String, charset: String?, contentType: ContentType): BodyClassifierResult?

}

/**
 * Classify body format based on content-type header. This goes horribly wrong when the header format differs from the actual format
 *
 * @param bodyFormatExtensions Extra body classifier extensions to take into account for mapping the content-type mime. These will be considered before the built-in values
 */
class HeaderBodyClassifier(private val bodyFormatExtensions: Iterable<BodyFormatClassifierExtension>) : BodyClassifier {

    override fun classifyFormat(message: NiddlerMessage): BodyClassifierResult {
        val contentTypeHeader = message.headers?.get("content-type")
        if (!contentTypeHeader.isNullOrEmpty()) {
            val contentTypeString = contentTypeHeader[0]
            val parsedContentType = ContentType.parse(contentTypeString)

            bodyFormatExtensions.forEach {
                val extensionFormat = it.classifyFormat(parsedContentType.mimeType, parsedContentType.charset?.name(), parsedContentType)
                if (extensionFormat != null)
                    return extensionFormat
            }

            val type = classifyFromMimeType(parsedContentType.mimeType, parsedContentType.charset?.name(), parsedContentType)
            if (type != null)
                return type
        }
        return BodyClassifierResult(BodyFormat.NONE, bodyParser = BinaryBodyParser())
    }

    private fun classifyFromMimeType(mimeType: String, charset: String?, contentType: ContentType): BodyClassifierResult? {
        val lowercased = mimeType.toLowerCase()
        return when (lowercased) {
            "application/json" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_JSON, mimeType, charset), JsonBodyParser())
            "application/xml", "text/xml", "application/dash+xml" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_XML, mimeType, charset), XmlBodyParser())
            "application/octet-stream" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_BINARY, mimeType, charset), BinaryBodyParser())
            "text/html", "application/xhtml+xml" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_HTML, mimeType, charset), HtmlBodyParser())
            "text/plain" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_PLAIN, mimeType, charset), PlainTextBodyParser())
            "application/x-www-form-urlencoded" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_FORM_ENCODED, mimeType, charset), URLEncodedBodyParser())
            "image/bmp", "image/png", "image/tiff", "image/jpg", "image/jpeg",
            "image/gif", "image/webp", "application/svg+xml" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_IMAGE, mimeType, charset), ImageBodyParser())
            //"multipart/form-data" -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_MULTIPART_FORM, mimeType, charset), MultiPartFormBodyParser(contentType))
            else -> when {
                lowercased.matches(Regex("text/.*")) -> BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_PLAIN, mimeType, charset), PlainTextBodyParser())
                else -> null
            }
        }
    }

}