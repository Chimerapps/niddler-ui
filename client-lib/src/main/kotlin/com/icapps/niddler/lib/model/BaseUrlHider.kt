package com.icapps.niddler.lib.model

import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Nicola Verbeeck
 *
 * Helper class to return urls which have their base stripped of
 */
class BaseUrlHider {

    private val hiddenBaseUrls = CopyOnWriteArrayList<String>()

    /**
     * Attempts to strip of the longest configured base url from the given url
     *
     * @param url   The url to strip the base of
     * @return The url with it's base stripped (if it was configured). Null if the base could not be stripped
     */
    fun getHiddenBaseUrl(url: String): String? {
        var longestMatch: String? = null
        hiddenBaseUrls.forEach { hiddenBase ->
            if (url.startsWith(hiddenBase)) {
                if (longestMatch == null || longestMatch!!.length < hiddenBase.length)
                    longestMatch = hiddenBase
            }
        }
        return longestMatch

    }

    fun getHiddenBaseUrls(): Collection<String> {
        return hiddenBaseUrls
    }

    fun updateHiddenBaseUrls(newList: Collection<String>) {
        hiddenBaseUrls.clear()
        hiddenBaseUrls.addAll(newList)
    }

    fun hideBaseUrl(baseUrl: String) {
        hiddenBaseUrls += baseUrl
    }

    fun unhideBaseUrl(baseUrl: String) {
        hiddenBaseUrls -= baseUrl
    }

}