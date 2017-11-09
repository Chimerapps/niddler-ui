package com.icapps.niddler.ui.export.har

/**
 * @author Nicola Verbeeck
 * @date 09/11/2017.
 */
data class Creator(
        val name: String,
        val version: String,
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
    }
}

data class Log(
        val version: String,
        val creator: Creator,
        val entries: List<Entry>,
        val comment: String? = null
)