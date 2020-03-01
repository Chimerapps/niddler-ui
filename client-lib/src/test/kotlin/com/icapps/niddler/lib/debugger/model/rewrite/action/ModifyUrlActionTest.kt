package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import org.junit.Assert.assertEquals
import org.junit.Test

class ModifyUrlActionTest {

    private val request = DebugRequest("https://www.example.com/api/upload.json", "GET")

    @Test
    fun testNormalFirst() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.URL, matchValue = "www.example.com", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "www.newexample.org", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyUrlAction(rule).apply(request)
        assertEquals("https://www.newexample.org/api/upload.json", request.url)
    }

    @Test
    fun testNormalAll() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.URL, matchValue = "www.example.com", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_ALL, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "https://www.newexample.org", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyUrlAction(rule).apply(request)
        assertEquals("https://www.newexample.org", request.url)
    }

    @Test
    fun testRegexAll() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.URL, matchValue = "(.*).json", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_ALL, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = true, caseSensitive = false, newHeader = null,
                newValue = "$1.test.json", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyUrlAction(rule).apply(request)
        assertEquals("https://www.example.com/api/upload.test.json", request.url)
    }

    @Test
    fun testRegexFirst() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.URL, matchValue = "(.*).json", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = true, caseSensitive = false, newHeader = null,
                newValue = "$1.test.json", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyUrlAction(rule).apply(request)
        assertEquals("https://www.example.com/api/upload.test.json", request.url)
    }

}