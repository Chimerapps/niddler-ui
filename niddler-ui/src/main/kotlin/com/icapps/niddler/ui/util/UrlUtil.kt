package com.icapps.niddler.ui.util

import java.net.URL
import java.net.URLDecoder
import java.util.LinkedList
import kotlin.collections.LinkedHashMap

/**
 * @author Nicola Verbeeck
 * @date 09/11/2017.
 */
class UrlUtil(private val fullUrl: String?) {

    private val internalUrl = if (fullUrl != null) URL(fullUrl) else null

    val queryString: String?
        get() {
            return internalUrl?.query
        }

    val url: String?
        get() {
            if (internalUrl?.query == null)
                return fullUrl

            return fullUrl?.substring(0, fullUrl.indexOf('?')) + '?' + queryString
        }

    val query: Map<String, List<String>>
        get() {
            val query = internalUrl?.query ?: return emptyMap()
            val params = LinkedHashMap<String, MutableList<String>>()
            val pairs = query.split("&")
            for (pair in pairs) {
                val idx = pair.indexOf("=")
                val key = if (idx > 0) URLDecoder.decode(pair.substring(0, idx), "UTF-8") else pair
                if (!params.containsKey(key)) {
                    params.put(key, LinkedList<String>())
                }
                val value = if (idx > 0 && pair.length > idx + 1) URLDecoder.decode(pair.substring(idx + 1), "UTF-8") else null
                if (value != null)
                    params[key]?.add(value)
            }
            return params
        }

}
