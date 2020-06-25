package com.icapps.niddler.lib.utils

import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * @author Nicola Verbeeck
 */
class UrlUtil(private val fullUrl: String?) {

    private val internalUrl = if (fullUrl != null) URL(fullUrl) else null

    val queryString: String? = internalUrl?.query

    val path: String? = internalUrl?.path

    val url: String?
        get() {
            if (internalUrl?.query == null)
                return fullUrl

            return fullUrl?.substring(0, fullUrl.indexOf('?')) + '?' + queryString
        }

    val query: Map<String, List<String>>
        get() {
            val query = queryString ?: return emptyMap()
            val params = LinkedHashMap<String, MutableList<String>>()
            val pairs = query.split("&")
            for (pair in pairs) {
                val idx = pair.indexOf("=")
                val key = if (idx > 0) URLDecoder.decode(pair.substring(0, idx), "UTF-8") else pair

                val value = if (idx > 0 && pair.length > idx + 1) URLDecoder.decode(pair.substring(idx + 1), "UTF-8") else null
                if (value != null)
                    params.getOrPut(key, { mutableListOf() }) += value
            }
            return params
        }

}

internal fun makeQueryString(queryData: Map<String, List<String>>): String {
    return buildString {
        queryData.forEach { (key, values) ->
            values.forEach { value ->
                if (length > 0) append('&')
                append(key).append('=').append(value)
            }
        }
    }
}

internal class UriBuilder(url: URI) {

    var scheme: String? = url.scheme
    var host: String? = url.host
    var port: Int = url.port
    var path: String? = url.path
    var query: String? = url.query
    var fragment: String? = url.fragment
    var userInfo: String? = url.userInfo

    fun build(): URI {
        return URI(scheme, userInfo, host, port, path, query, fragment)
    }

}

internal fun URI.newBuilder(): UriBuilder {
    return UriBuilder(this)
}
