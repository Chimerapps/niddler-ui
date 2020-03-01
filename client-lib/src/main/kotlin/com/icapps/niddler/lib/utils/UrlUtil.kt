package com.icapps.niddler.lib.utils

import java.net.URL
import java.net.URLDecoder

/**
 * @author Nicola Verbeeck
 */
class UrlUtil(private val fullUrl: String?) {

    private val internalUrl = if (fullUrl != null) URL(fullUrl) else null

    val queryString: String? = internalUrl?.query

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

class UrlBuilder(url: URL) {

    var scheme: String = url.protocol
    var host: String = url.host
    var port: Int = url.port
    var file: String = url.file

    fun build(): URL {
        return URL(scheme, host, port, file)
    }

}

fun URL.newBuilder(): UrlBuilder {
    return UrlBuilder(this)
}
