package com.icapps.niddler.lib.utils

import com.google.gson.JsonParser
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader
import java.net.URLDecoder
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author nicolaverbeeck
 */
class BodyClassifier(private val contentType: String?, private val bodyBytes: ByteArray?) {

    private companion object {
        private const val SPACE = 32.toByte()
        private const val LF = 10.toByte()
        private const val CR = 13.toByte()
        private const val TAB = 9.toByte()
    }

    fun determineBodyType(): ConcreteBody? {
        if (bodyBytes == null || bodyBytes.isEmpty())
            return determineFromMime(contentType, bytes = null)

        val fromMime = determineFromMime(contentType, bodyBytes)
        if (fromMime == null || fromMime.type == BodyFormatType.FORMAT_BINARY)
            return determineBodyFromContent(bodyBytes) ?: fromMime
        return fromMime
    }

    private fun determineFromMime(contentType: String?, bytes: ByteArray?): ConcreteBody? {
        if (contentType == null || contentType.isEmpty())
            return null

        when (contentType.toLowerCase()) {
            "application/json" ->
                return ConcreteBody(BodyFormatType.FORMAT_JSON, contentType, examineJson(bytes))
            "application/xml", "text/xml", "application/dash+xml" ->
                return ConcreteBody(BodyFormatType.FORMAT_XML, contentType, examineXML(bytes))
            "application/octet-stream" ->
                return ConcreteBody(BodyFormatType.FORMAT_BINARY, contentType, bytes)
            "text/html" ->
                return ConcreteBody(BodyFormatType.FORMAT_HTML, contentType, examineXML(bytes))
            "text/plain" ->
                return ConcreteBody(BodyFormatType.FORMAT_PLAIN, contentType, bytes?.let { String(it) })
            "application/x-www-form-urlencoded" ->
                return ConcreteBody(BodyFormatType.FORMAT_FORM_ENCODED, contentType, examineFormEncoded(bytes))
            "image/bmp", "image/png", "image/tiff", "image/jpg", "image/jpeg", "image/gif" ->
                return ConcreteBody(BodyFormatType.FORMAT_IMAGE, contentType,
                        bytes?.let {
                            ImageIO.read(ByteArrayInputStream(it))
                        })
            "image/webp" ->
                return ConcreteBody(BodyFormatType.FORMAT_IMAGE, contentType, bytes?.let {
                    readWebPImage(it)
                })
            "application/svg+xml" ->
                return ConcreteBody(BodyFormatType.FORMAT_IMAGE, contentType, examineXML(bytes)) //TODO render SVG
        }
        return null
    }

    private fun determineBodyFromContent(content: ByteArray): ConcreteBody? {
        val firstReasonableTextByte = findFirstNonBlankByte(content)
        when (firstReasonableTextByte) {
            '{'.toByte(), '['.toByte() ->
                return ConcreteBody(BodyFormatType.FORMAT_JSON, "application/json", examineJson(content))
            '<'.toByte() ->
                examineXML(content)?.let { xmlData ->
                    return if (firstBytesContainHtml(content, "html"))
                        ConcreteBody(BodyFormatType.FORMAT_HTML, "text/html", xmlData)
                    else
                        ConcreteBody(BodyFormatType.FORMAT_XML, "application/xml", xmlData) //TODO image svg
                }
        }
        return null
    }

    private fun findFirstNonBlankByte(bytes: ByteArray): Byte? {
        val index = bytes.indexOfFirst { it != SPACE || it != CR || it != LF || it != TAB }
        return bytes.getOrNull(index)
    }

    private fun examineJson(bodyAsBytes: ByteArray?): Any? {
        if (bodyAsBytes == null || bodyAsBytes.isEmpty()) return null
        try {
            return JsonParser().parse(InputStreamReader(ByteArrayInputStream(bodyAsBytes), Charsets.UTF_8))
        } catch (e: Exception) {
            return null
        }
    }

    private fun examineXML(bodyAsBytes: ByteArray?): Any? {
        if (bodyAsBytes == null || bodyAsBytes.isEmpty()) return null
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(bodyAsBytes))
        } catch (e: Exception) {
            return null
        }
    }

    private fun examineFormEncoded(bodyAsBytes: ByteArray?): Any? {
        if (bodyAsBytes == null || bodyAsBytes.isEmpty()) return null

        val map: MutableMap<String, String> = mutableMapOf()
        String(bodyAsBytes).split('&').forEach {
            val parts = it.split('=')
            val key = URLDecoder.decode(parts[0], "UTF-8")
            val value = URLDecoder.decode(parts[1], "UTF-8")
            map[key] = value
        }
        return map
    }

    private fun firstBytesContainHtml(bytes: ByteArray, string: String): Boolean {
        return String(bytes, 0, Math.min(bytes.size, 32)).contains(string, true)
    }

    private fun readWebPImage(bytes: ByteArray): BufferedImage? {
        if (!File("/usr/local/bin/dwebp").exists())
            return null
        val source = File.createTempFile("tmp_img", "dat")
        source.writeBytes(bytes)
        val converted = File.createTempFile("tmp_img", "png")
        val proc = ProcessBuilder()
                .command("/usr/local/bin/dwebp", source.absolutePath, "-o", converted.absolutePath) //TODO fix
                .start()
        proc.waitFor()
        try {
            return ImageIO.read(converted)
        } finally {
            source.delete()
            converted.delete()
        }
    }
}

data class ConcreteBody(
        val type: BodyFormatType,
        val rawType: String,
        val data: Any?
)


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