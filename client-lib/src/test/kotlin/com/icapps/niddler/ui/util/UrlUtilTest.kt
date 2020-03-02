package com.icapps.niddler.ui.util

import com.icapps.niddler.lib.utils.UrlUtil
import com.icapps.niddler.lib.utils.makeQueryString
import com.icapps.niddler.lib.utils.newBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

/**
 * @author Nicola Verbeeck
 * @date 10/11/2017.
 */
class UrlUtilTest {

    @Test
    fun getQueryString() {
        var url = UrlUtil("https://www.google.com?q=3&g=3133&s=%20w")
        assertEquals("q=3&g=3133&s=%20w", url.queryString)

        url = UrlUtil("https://www.google.com?q=3&g=3133&s=%20w#2")
        assertEquals("q=3&g=3133&s=%20w", url.queryString)

        url = UrlUtil("https://www.google.com")
        assertEquals(null, url.queryString)
    }

    @Test
    fun getUrl() {
        var url = UrlUtil("https://www.google.com?q=3&g=3133&s=%20w")
        assertEquals("https://www.google.com?q=3&g=3133&s=%20w", url.url)

        url = UrlUtil("https://www.google.com?q=3&g=3133&s=%20w#2")
        assertEquals("https://www.google.com?q=3&g=3133&s=%20w", url.url)

        url = UrlUtil("https://www.google.com")
        assertEquals("https://www.google.com", url.url)
    }

    @Test
    fun getQuery() {
        val url = UrlUtil("https://www.google.com?q=3&g=3133&s=%20w#2")

        val query = url.query
        assertTrue(query.containsKey("q"))
        assertTrue(query.containsKey("g"))
        assertTrue(query.containsKey("s"))

        assertEquals("3", query["q"]?.get(0))
        assertEquals("3133", query["g"]?.get(0))
        assertEquals(" w", query["s"]?.get(0))
    }

    @Test
    fun buildQuery() {
        val string = URI("https://www.google.com").newBuilder()
                .also {
                    it.query = makeQueryString(mapOf("q" to listOf("3"), "g" to listOf("3133"), "s" to listOf(" w")))
                    it.fragment = "2"
                }.build().toString()

        assertEquals("https://www.google.com?q=3&g=3133&s=%20w#2", string)
    }

    @Test
    fun buildQueryMultiple() {
        val string = URI("https://www.google.com").newBuilder()
                .also {
                    it.query = makeQueryString(mapOf("q" to listOf("3", "abc"), "g" to listOf("3133"), "s" to listOf(" w")))
                    it.fragment = "2"
                }.build().toString()

        assertEquals("https://www.google.com?q=3&q=abc&g=3133&s=%20w#2", string)
    }

}