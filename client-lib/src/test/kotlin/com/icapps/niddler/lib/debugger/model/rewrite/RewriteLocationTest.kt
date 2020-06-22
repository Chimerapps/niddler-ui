package com.icapps.niddler.lib.debugger.model.rewrite

import com.icapps.niddler.lib.debugger.model.configuration.DebugLocation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewriteLocationTest {

    private companion object {
        val protocols = arrayOf("http", null)
        val hosts = arrayOf("test.com", null)
        val ports = arrayOf("8888", null)
        val paths = arrayOf("/api/v1/NL/getItems", null)
        val queries = arrayOf("matchAll=true", null)
    }

    @Test
    fun testGenericToRegex() {
        for (protocol in protocols) {
            for (host in hosts) {
                for (port in ports) {
                    for (path in paths) {
                        for (query in queries) {
                            checkMatches(DebugLocation(protocol, host, port, path, query).asRegex())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testWildcardToRegex() {
        assertTrue(Regex(DebugLocation("http", null, null, null, null).asRegex()).matches("http://test.com:8888/api/v1/NL/getItems?matchAll=true#fragment=1029"))
        assertFalse(Regex(DebugLocation("http", "test.com", null, null, null).asRegex()).matches("http://test.come:8888/api/v1/NL/getItems?matchAll=true#fragment=1029"))
        assertFalse(Regex(DebugLocation("http", null, "8888", null, null).asRegex()).matches("http://test.com:88881/api/v1/NL/getItems?matchAll=true#fragment=1029"))
        assertFalse(Regex(DebugLocation("http", null, null, "api/v1/NL/getItems", null).asRegex()).matches("http://test.com:8888/api/v1/NL/getItems/f?matchAll=true#fragment=1029"))
        assertFalse(Regex(DebugLocation("http", null, null, null, "matchAll=true").asRegex()).matches("http://test.com:8888/api/v1/NL/getItems/f?matchAll=true&matchNone=false#fragment=1029"))

        assertTrue(Regex(DebugLocation(null, "test.com", null, null, null).asRegex()).matches("https://test.com/posts"))
    }

    private fun checkMatches(regex: String) {
        assertTrue("Regex: $regex does not match", Regex(regex).matches("http://test.com:8888/api/v1/NL/getItems?matchAll=true#fragment=1029"))
    }

}