package com.icapps.niddler.lib.export

import com.icapps.niddler.lib.export.har.Cache
import com.icapps.niddler.lib.export.har.Content
import com.icapps.niddler.lib.export.har.ContentBuilder
import com.icapps.niddler.lib.export.har.Creator
import com.icapps.niddler.lib.export.har.Entry
import com.icapps.niddler.lib.export.har.Header
import com.icapps.niddler.lib.export.har.Param
import com.icapps.niddler.lib.export.har.PostData
import com.icapps.niddler.lib.export.har.PostDataBuilder
import com.icapps.niddler.lib.export.har.QueryParameter
import com.icapps.niddler.lib.export.har.Request
import com.icapps.niddler.lib.export.har.Response
import com.icapps.niddler.lib.export.har.StreamingHarWriter
import com.icapps.niddler.lib.export.har.Timings
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.LinkedMessageHolder
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ObservableLinkedMessagesView
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.utils.UrlUtil
import java.io.OutputStream
import java.util.Date

/**
 * @author Nicola Verbeeck
 */
class HarExport<T : ParsedNiddlerMessage>() : Exporter<T> {

    override fun export(target: OutputStream, messages: NiddlerMessageStorage<T>, filter: NiddlerMessageStorage.Filter<T>?) {
        val buffered = target.buffered()
        val writer = StreamingHarWriter(target = buffered,
                creator = Creator("Niddler", "1.0"))

        exportTo(messages.messagesLinked.newView(filter, rootMessageListener = null), writer)

        writer.close()
    }

    private fun exportTo(messages: ObservableLinkedMessagesView<T>, writer: StreamingHarWriter) {
        messages.snapshot().forEach {
            exportTo(it, writer)
        }
    }

    private fun exportTo(niddlerMessages: LinkedMessageHolder<T>, writer: StreamingHarWriter) {
        val request = niddlerMessages.request ?: return
        val response = niddlerMessages.responses.firstOrNull() ?: return

        val time = (response.timestamp - request.timestamp).toDouble()
        val harEntry = Entry(
                startedDateTime = Entry.format(Date(request.timestamp)),
                time = time,
                request = makeRequest(request),
                response = makeResponse(response),
                cache = Cache(),
                timings = extractTimings(response, time)
        )

        writer.addEntry(harEntry)
    }

    private fun makeRequest(niddlerMessage: T): Request {
        val urlUtil = UrlUtil(niddlerMessage.url)

        return Request(
                method = niddlerMessage.method ?: "",
                url = urlUtil.url ?: "",
                httpVersion = niddlerMessage.httpVersion?.toUpperCase() ?: "HTTP/1.1",
                headers = makeHeaders(niddlerMessage),
                queryString = urlUtil.query.map {
                    QueryParameter(name = it.key, value = it.value.joinToString(","))
                },
                postData = extractPostData(niddlerMessage)
        )
    }

    private fun makeResponse(niddlerMessage: T): Response {

        return Response(
                status = niddlerMessage.statusCode ?: 0,
                statusText = niddlerMessage.statusLine ?: "",
                httpVersion = niddlerMessage.httpVersion?.toUpperCase() ?: "HTTP/1.1",
                content = extractContent(niddlerMessage),
                headers = makeHeaders(niddlerMessage)
        )
    }

    private fun makeHeaders(message: T): List<Header> {
        return message.headers?.map {
            Header(name = it.key, value = it.value.joinToString(","))
        } ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractPostData(message: T): PostData? {
        if (message.body == null)
            return null

        val builder = PostDataBuilder()
        val format = message.bodyFormat
        when (format.type) {
            BodyFormatType.FORMAT_JSON -> builder.withMime(BodyFormatType.FORMAT_JSON.verbose).withText(message.getBodyAsString(format.encoding) ?: "")
            BodyFormatType.FORMAT_XML -> builder.withMime(BodyFormatType.FORMAT_XML.verbose).withText(message.getBodyAsString(format.encoding) ?: "")
            BodyFormatType.FORMAT_PLAIN -> builder.withMime(BodyFormatType.FORMAT_PLAIN.verbose).withText(message.getBodyAsString(format.encoding) ?: "")
            BodyFormatType.FORMAT_IMAGE -> builder.withMime(format.rawMimeType ?: "").withText(message.bodyAsNormalBase64 ?: "")
            BodyFormatType.FORMAT_BINARY -> builder.withMime(format.rawMimeType ?: "").withText(message.bodyAsNormalBase64 ?: "")
            BodyFormatType.FORMAT_HTML -> builder.withMime(format.rawMimeType ?: "").withText(message.getBodyAsString(format.encoding ?: "UTF-8") ?: "")
            BodyFormatType.FORMAT_EMPTY -> return null
            BodyFormatType.FORMAT_FORM_ENCODED -> builder.withMime("application/x-www-form-urlencoded")
                    .withParams((message.bodyData as Map<String, List<String>>).flatMap { (key, value) -> value.map { entry -> Param(name = key, value = entry) } })
        }
        return builder.build()
    }

    private fun extractContent(message: T): Content {
        if (message.body == null)
            return Content(size = -1, mimeType = "", text = null, encoding = null)

        val builder = ContentBuilder()
        val format = message.bodyFormat
        when (format.type) {
            BodyFormatType.FORMAT_JSON -> builder.withMime(BodyFormatType.FORMAT_JSON.verbose).withText(message.getBodyAsString(format.encoding))
            BodyFormatType.FORMAT_XML -> builder.withMime(BodyFormatType.FORMAT_XML.verbose).withText(message.getBodyAsString(format.encoding))
            BodyFormatType.FORMAT_PLAIN -> builder.withMime(BodyFormatType.FORMAT_PLAIN.verbose).withText(message.getBodyAsString(format.encoding))
            BodyFormatType.FORMAT_IMAGE -> builder.withMime(format.rawMimeType ?: "").withText(message.bodyAsNormalBase64).withEncoding("base64")
            BodyFormatType.FORMAT_BINARY -> builder.withMime(format.rawMimeType ?: "").withText(message.bodyAsNormalBase64).withEncoding("base64")
            BodyFormatType.FORMAT_HTML -> builder.withMime(format.rawMimeType ?: "").withText(message.bodyAsNormalBase64).withEncoding("base64")
            BodyFormatType.FORMAT_EMPTY -> builder.withMime("").withText("")
            else -> builder.withMime("").withText("")
        }
        builder.withSize(message.headers?.get("content-length")?.getOrNull(0)?.toLongOrNull() ?: message.getBodyAsBytes?.size?.toLong() ?: -1L)
        return builder.build()
    }

    fun extractTimings(response: T, totalRequestTime: Double): Timings {
        var writeTime = response.writeTime?.toDouble() ?: 0.0
        val waitTime = response.waitTime?.toDouble() ?: 0.0
        val readTime = response.readTime?.toDouble() ?: 0.0

        val totalKnownTime = writeTime + waitTime + readTime
        if (totalKnownTime < totalRequestTime) {
            writeTime += (totalRequestTime - totalKnownTime)
        }

        return Timings(
                writeTime,
                waitTime,
                readTime
        )
    }

}