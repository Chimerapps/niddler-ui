package com.icapps.niddler.ui.export

import com.icapps.niddler.ui.model.BodyFormatType
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.util.UrlUtil
import com.smartbear.har.builder.*
import com.smartbear.har.creator.DefaultHarStreamWriter
import com.smartbear.har.creator.HarStreamWriter
import com.smartbear.har.model.*
import java.io.File
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 09/11/2017.
 */
class HarExport(private val targetFile: File) {

    fun export(messages: Map<String, List<ParsedNiddlerMessage>>) {
        val writer = DefaultHarStreamWriter.Builder()
                .withCreator(HarCreatorBuilder().withName("Niddler").withVersion("1").build())
                .withOutputFile(targetFile)
                .withUsePrettyPrint(true)
                .build()

        exportTo(messages, writer)

        writer.closeHar()
    }

    private fun exportTo(messages: Map<String, List<ParsedNiddlerMessage>>, writer: HarStreamWriter) {
        messages.forEach {
            exportTo(it.value, writer)
        }
    }

    private fun exportTo(niddlerMessages: List<ParsedNiddlerMessage>, writer: HarStreamWriter) {
        val request = extractRequest(niddlerMessages)
        val response = extractResponse(niddlerMessages)

        val builder = HarEntryBuilder()
        if (request != null)
            builder.withRequest(request)
        if (response != null)
            builder.withResponse(response)

        builder.withStartedDateTime(niddlerMessages.find { it.isRequest }?.timestamp?.let { Date(it) })
        builder.withCache(HarCacheBuilder().build())
        builder.withTimings(HarTimingsBuilder().
                withSend(0)
                .withWait(0)
                .withReceive(0)
                .build())

        if (request != null || response != null)
            writer.addEntry(builder.build())
    }

    private fun extractRequest(niddlerMessages: List<ParsedNiddlerMessage>): HarRequest? {
        val request = niddlerMessages.find { it.isRequest } ?: return null

        val urlUtil = UrlUtil(request.url)
        val builder = HarRequestBuilder()
        builder.withUrl(urlUtil.url)
                .withMethod(request.method)
                .withHeaders(makeHeaders(request))
                .withPostData(extractPostData(request))
                .withHttpVersion("1")

        if (urlUtil.queryString != null)
            builder.withQueryString(urlUtil.queryString)

        return builder.build()
    }

    private fun extractResponse(niddlerMessages: List<ParsedNiddlerMessage>): HarResponse? {
        val response = niddlerMessages.first { !it.isRequest } ?: return null

        val builder = HarResponseBuilder()
        return builder.withHeaders(makeHeaders(response))
                .withStatus(response.statusCode ?: 0)
                .withContent(extractContent(response))
                .withStatusText("-")
                .withHttpVersion("1")
                .withRedirectURL("-")
                .withHeadersSize(-1)
                .withBodySize(-1)
                .build()
    }

    private fun makeHeaders(message: ParsedNiddlerMessage): List<HarHeader> {
        return message.headers?.map {
            HarHeaderBuilder().withName(it.key).withValues(it.value).build()
        } ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractPostData(message: ParsedNiddlerMessage): HarPostData? {
        if (message.message.body == null)
            return null

        val builder = HarPostDataBuilder()
        when (message.bodyFormat.type) {
            BodyFormatType.FORMAT_JSON -> builder.withMimeType(BodyFormatType.FORMAT_JSON.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_XML -> builder.withMimeType(BodyFormatType.FORMAT_XML.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_PLAIN -> builder.withMimeType(BodyFormatType.FORMAT_PLAIN.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_IMAGE -> return null
            BodyFormatType.FORMAT_BINARY -> return null
            BodyFormatType.FORMAT_HTML -> return null
            BodyFormatType.FORMAT_EMPTY -> return null
            BodyFormatType.FORMAT_FORM_ENCODED -> builder.withParams((message.body as Map<String, String>).map { HarParamsBuilder().withName(it.key).withValue(it.value).build() })
            else -> return null
        }
        return builder.build()
    }

    private fun extractContent(message: ParsedNiddlerMessage): HarContent? {
        if (message.message.body == null)
            return null

        val builder = HarContentBuilder()
        when (message.bodyFormat.type) {
            BodyFormatType.FORMAT_JSON -> builder.withMimeType(BodyFormatType.FORMAT_JSON.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_XML -> builder.withMimeType(BodyFormatType.FORMAT_XML.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_PLAIN -> builder.withMimeType(BodyFormatType.FORMAT_PLAIN.verbose).withText(message.message.getBodyAsString(message.bodyFormat.encoding))
            BodyFormatType.FORMAT_IMAGE -> builder.withMimeType(message.bodyFormat.subtype ?: "").withText(message.message.body)
            BodyFormatType.FORMAT_BINARY -> builder.withMimeType(message.bodyFormat.subtype ?: "").withText(message.message.body)
            BodyFormatType.FORMAT_HTML -> builder.withMimeType(message.bodyFormat.subtype ?: "").withText(message.message.body)
            BodyFormatType.FORMAT_EMPTY -> builder.withMimeType("").withText("")
            else -> builder.withMimeType("").withText("")
        }
        builder.withSize(-1)
        return builder.build()
    }


}