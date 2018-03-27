package com.icapps.niddler.lib.model.classifier

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.BodyFormat
import org.apache.http.entity.ContentType

/**
 * @author nicolaverbeeck
 */
interface SimpleBodyClassifier {

    fun classifyFormat(message: NiddlerMessage): BodyFormat

}

class HeaderBodyClassifier : SimpleBodyClassifier {

    override fun classifyFormat(message: NiddlerMessage): BodyFormat {
        val contentTypeHeader = message.headers?.get("content-type")
        if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
            val contentTypeString = contentTypeHeader[0]
            val parsedContentType = ContentType.parse(contentTypeString)
            val type = classifyFromMimeType(parsedContentType.mimeType)
            if (type != null)
                return BodyFormat(type, parsedContentType.mimeType, parsedContentType.charset?.name())
        }
        return BodyFormat.NONE
    }

    protected fun classifyFromMimeType(mimeType: String): BodyFormatType? {
        return when (mimeType.toLowerCase()) {
            "application/json" ->
                BodyFormatType.FORMAT_JSON
            "application/xml", "text/xml", "application/dash+xml" ->
                BodyFormatType.FORMAT_XML
            "application/octet-stream" ->
                BodyFormatType.FORMAT_BINARY
            "text/html" ->
                BodyFormatType.FORMAT_HTML
            "text/plain" ->
                BodyFormatType.FORMAT_PLAIN
            "application/x-www-form-urlencoded" ->
                BodyFormatType.FORMAT_FORM_ENCODED
            "image/bmp", "image/png", "image/tiff", "image/jpg", "image/jpeg", "image/gif",
            "image/webp", "application/svg+xml" ->
                BodyFormatType.FORMAT_IMAGE
            else -> null
        }
    }

}