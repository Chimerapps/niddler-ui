package com.icapps.niddler.lib.model

import java.nio.charset.Charset

/**
 * @author Nicola Verbeeck
 */
interface BodyParser<T : Any> {

    companion object {
        fun makeCharset(bodyFormat: BodyFormat): Charset {
            if (bodyFormat.encoding == null)
                return Charsets.UTF_8
            return try {
                Charset.forName(bodyFormat.encoding)
            } catch (e: Throwable) {
                Charsets.UTF_8
            }
        }
    }

    fun parse(bodyFormat: BodyFormat, bytes: ByteArray): T?

}

/**
 * @author Nicola Verbeeck
 */
data class BodyFormat(val type: BodyFormatType, val rawMimeType: String?, val encoding: String?) {
    companion object {
        val NONE = BodyFormat(BodyFormatType.FORMAT_EMPTY, null, null)
        val UNKNOWN = BodyFormat(BodyFormatType.FORMAT_BINARY, null, null)
    }

    override fun toString(): String {
        return type.verbose
    }
}

/**
 * @author Nicola Verbeeck
 */
data class BodyFormatType(val verbose: String) {
    companion object {
        val FORMAT_JSON = BodyFormatType("application/json")
        val FORMAT_XML = BodyFormatType("application/xml")
        val FORMAT_PLAIN = BodyFormatType("text/plain")
        val FORMAT_IMAGE = BodyFormatType("image")
        val FORMAT_BINARY = BodyFormatType("binary")
        val FORMAT_HTML = BodyFormatType("text/html")
        val FORMAT_EMPTY = BodyFormatType("")
        val FORMAT_FORM_ENCODED = BodyFormatType("x-www-form-urlencoded")
    }
}