package com.icapps.niddler.lib.model

import java.util.concurrent.CopyOnWriteArrayList

class BaseUrlHider {

    private val hiddenBaseUrls = CopyOnWriteArrayList<String>()

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