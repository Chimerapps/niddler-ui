package com.icapps.niddler.ui.export.har

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 09/11/2017.
 */
data class Creator(
        val name: String,
        val version: String = "1.2",
        val comment: String? = null
)

data class Header(
        val name: String,
        val value: String,
        val comment: String? = null
)

typealias QueryParameter = Header

data class Param(
        val name: String,
        val value: String? = null,
        val fileName: String? = null,
        val contentType: String? = null,
        val comment: String? = null
)

data class PostData(
        val mimeType: String,
        val params: List<Param>,
        val text: String,
        val comment: String? = null
)

class PostDataBuilder {
    var mimeType: String? = null
    var params: List<Param> = emptyList()
    var text: String? = null
    var comment: String? = null

    fun withMime(mime: String): PostDataBuilder {
        mimeType = mime
        return this
    }

    fun withParams(params: List<Param>): PostDataBuilder {
        this.params = params
        return this
    }

    fun withText(text: String): PostDataBuilder {
        this.text = text
        return this
    }

    fun withComment(comment: String?): PostDataBuilder {
        this.comment = comment
        return this
    }

    fun build(): PostData {
        return PostData(mimeType = mimeType ?: "",
                params = params,
                text = text ?: "",
                comment = comment)
    }
}

data class Request(
        val method: String,
        val url: String,
        val httpVersion: String,
        val headers: List<Header>,
        val queryString: List<QueryParameter>,
        val postData: PostData?,
        val cookies: List<Any> = emptyList(),
        val headersSize: Long = 1,
        val bodySize: Long = -1,
        val comment: String? = null
)

data class Content(
        val size: Long,
        val mimeType: String,
        val text: String?,
        val encoding: String?,
        val comment: String? = null
)

class ContentBuilder {
    var size: Long? = null
    var mimeType: String? = null
    var text: String? = null
    var encoding: String? = null
    var comment: String? = null

    fun withSize(size: Long): ContentBuilder {
        this.size = size
        return this
    }

    fun withMime(mime: String): ContentBuilder {
        this.mimeType = mime
        return this
    }

    fun withText(text: String?): ContentBuilder {
        this.text = text
        return this
    }

    fun withEncoding(encoding: String?): ContentBuilder {
        this.encoding = encoding
        return this
    }

    fun comment(comment: String?): ContentBuilder {
        this.comment = comment
        return this
    }

    fun build(): Content {
        return Content(
                size = size ?: -1,
                mimeType = mimeType ?: "",
                text = text,
                encoding = encoding,
                comment = comment
        )
    }
}

data class Response(
        val status: Int,
        val statusText: String,
        val httpVersion: String,
        val content: Content,
        val headers: List<Header>,
        val redirectURL: String = "",
        val cookies: List<Any> = emptyList(),
        val headersSize: Long = -1,
        val bodySize: Long = -1,
        val comment: String? = null
)

class Cache

data class Timings(
        val send: Long,
        val wait: Long,
        val receive: Long,
        val comment: String? = null
)

data class Entry(
        val startedDateTime: String,
        val time: Long,
        val request: Request,
        val response: Response,
        val cache: Cache,
        val timings: Timings,
        val comment: String? = null
) {
    companion object {
        val TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"

        fun format(date: Date): String {
            return SimpleDateFormat(TIME_FORMAT).format(date)
        }
    }
}

data class Log(
        val version: String,
        val creator: Creator,
        val entries: List<Entry>,
        val comment: String? = null
)