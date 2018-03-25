package com.icapps.niddler.ui.export

import com.icapps.niddler.ui.export.har.*
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.utils.BodyFormatType
import com.icapps.niddler.ui.util.UrlUtil
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 09/11/2017.
 */
class HarExport(private val targetFile: File) {

    fun export(messages: Map<String, List<ParsedNiddlerMessage>>) {
        val writer = StreamingHarWriter(target = FileOutputStream(targetFile).buffered(),
                creator = Creator("Niddler", "1.0"))

        exportTo(messages, writer)

        writer.close()
    }

    private fun exportTo(messages: Map<String, List<ParsedNiddlerMessage>>, writer: StreamingHarWriter) {
        messages.forEach {
            exportTo(it.value, writer)
        }
    }

    private fun exportTo(niddlerMessages: List<ParsedNiddlerMessage>, writer: StreamingHarWriter) {
        val request = niddlerMessages.find { it.isRequest } ?: return
        val response = niddlerMessages.firstOrNull { !it.isRequest } ?: return

        val harEntry = Entry(
                startedDateTime = Entry.format(request.timestamp.let { Date(it) }),
                time = response.timestamp - request.timestamp,
                request = makeRequest(request),
                response = makeResponse(response),
                cache = Cache(),
                timings = extractTimings(request, response)
        )

        writer.addEntry(harEntry)
    }

    private fun makeRequest(niddlerMessage: ParsedNiddlerMessage): Request {
        val urlUtil = UrlUtil(niddlerMessage.url)

        return Request(
                method = niddlerMessage.method ?: "",
                url = urlUtil.url ?: "",
                httpVersion = niddlerMessage.message.httpVersion?.toUpperCase() ?: "HTTP/1.1",
                headers = makeHeaders(niddlerMessage),
                queryString = urlUtil.query.map {
                    QueryParameter(name = it.key, value = it.value.joinToString(","))
                },
                postData = extractPostData(niddlerMessage)
        )
    }

    private fun makeResponse(niddlerMessage: ParsedNiddlerMessage): Response {

        return Response(
                status = niddlerMessage.statusCode ?: 0,
                statusText = niddlerMessage.message.statusLine ?: "",
                httpVersion = niddlerMessage.message.httpVersion?.toUpperCase() ?: "HTTP/1.1",
                content = extractContent(niddlerMessage),
                headers = makeHeaders(niddlerMessage)
        )
    }

    private fun makeHeaders(message: ParsedNiddlerMessage): List<Header> {
        return message.headers?.map {
            Header(name = it.key, value = it.value.joinToString(","))
        } ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractPostData(message: ParsedNiddlerMessage): PostData? {
        if (message.message.body == null)
            return null

        val builder = PostDataBuilder()
        when (message.bodyFormat.type) {
            BodyFormatType.FORMAT_JSON -> builder.withMime(BodyFormatType.FORMAT_JSON.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding)
                    ?: "")
            BodyFormatType.FORMAT_XML -> builder.withMime(BodyFormatType.FORMAT_XML.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding)
                    ?: "")
            BodyFormatType.FORMAT_PLAIN -> builder.withMime(BodyFormatType.FORMAT_PLAIN.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding)
                    ?: "")
            BodyFormatType.FORMAT_IMAGE -> builder.withMime(message.bodyFormat.subtype
                    ?: "").withText(message.message.bodyAsNormalBase64 ?: "")
            BodyFormatType.FORMAT_BINARY -> builder.withMime(message.bodyFormat.subtype
                    ?: "").withText(message.message.bodyAsNormalBase64 ?: "")
            BodyFormatType.FORMAT_HTML -> builder.withMime(message.bodyFormat.subtype
                    ?: "").withText(message.message.getBodyAsString("UTF-8") ?: "")
            BodyFormatType.FORMAT_EMPTY -> return null
            BodyFormatType.FORMAT_FORM_ENCODED -> builder.withParams((message.body as Map<String, String>).map { Param(name = it.key, value = it.value) })
        }
        return builder.build()
    }

    private fun extractContent(message: ParsedNiddlerMessage): Content {
        if (message.message.body == null)
            return Content(size = -1, mimeType = "", text = null, encoding = null)

        val builder = ContentBuilder()
        when (message.bodyFormat.type) {
            BodyFormatType.FORMAT_JSON -> builder.withMime(BodyFormatType.FORMAT_JSON.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_XML -> builder.withMime(BodyFormatType.FORMAT_XML.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_PLAIN -> builder.withMime(BodyFormatType.FORMAT_PLAIN.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_IMAGE -> builder.withMime(message.bodyFormat.subtype
                    ?: "").withText(message.message.bodyAsNormalBase64).withEncoding("base64")
            BodyFormatType.FORMAT_BINARY -> builder.withMime(message.bodyFormat.subtype
                    ?: "").withText(message.message.bodyAsNormalBase64).withEncoding("base64")
            BodyFormatType.FORMAT_HTML -> builder.withMime(message.bodyFormat.subtype
                    ?: "").withText(message.message.bodyAsNormalBase64).withEncoding("base64")
            BodyFormatType.FORMAT_EMPTY -> builder.withMime("").withText("")
            else -> builder.withMime("").withText("")
        }
        builder.withSize(-1)
        return builder.build()
    }

    fun extractTimings(request: ParsedNiddlerMessage, response: ParsedNiddlerMessage): Timings {
        return Timings(
                response.message.writeTime?.toLong() ?: 0L,
                response.message.waitTime?.toLong() ?: 0L,
                response.message.readTime?.toLong() ?: 0L
        )
    }

}