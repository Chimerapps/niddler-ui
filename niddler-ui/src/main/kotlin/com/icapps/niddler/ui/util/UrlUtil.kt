package com.icapps.niddler.ui.util

import java.net.URL


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
            return fullUrl?.replace(internalUrl.query, "")
        }

}
