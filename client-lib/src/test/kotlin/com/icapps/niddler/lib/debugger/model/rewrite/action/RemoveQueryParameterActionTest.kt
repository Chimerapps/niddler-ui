package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveQueryParameterActionTest {

    @Test
    fun testRemoveMatchingName() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.REMOVE_QUERY_PARAM, matchHeader = "token", matchHeaderRegex = false,
                newValueRegex = false, newValue = null, matchValue = null, newHeader = null, caseSensitive = true, matchRequest = true,
                matchResponse = false, matchValueRegex = false, matchWholeValue = false, newHeaderRegex = false, replaceType = ReplaceType.REPLACE_ALL)

        val action = RemoveQueryParameterAction(rule)
        val request = DebugRequest("https://www.example.com?token=123&type=auth")

        assertEquals("https://www.example.com?type=auth", action.apply(request).url)
    }

    @Test
    fun testRemoveNoMatchingName() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.REMOVE_QUERY_PARAM, matchHeader = "tokenNoMatch", matchHeaderRegex = false,
                newValueRegex = false, newValue = null, matchValue = null, newHeader = null, caseSensitive = true, matchRequest = true,
                matchResponse = false, matchValueRegex = false, matchWholeValue = false, newHeaderRegex = false, replaceType = ReplaceType.REPLACE_ALL)

        val action = RemoveQueryParameterAction(rule)
        val request = DebugRequest("https://www.example.com?token=123&type=auth")

        assertEquals("https://www.example.com?token=123&type=auth", action.apply(request).url)
    }

    @Test
    fun testRemoveMatchingValue() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.REMOVE_QUERY_PARAM, matchHeader = null, matchHeaderRegex = false,
                newValueRegex = false, newValue = null, matchValue = "123", newHeader = null, caseSensitive = true, matchRequest = true,
                matchResponse = false, matchValueRegex = false, matchWholeValue = false, newHeaderRegex = false, replaceType = ReplaceType.REPLACE_ALL)

        val action = RemoveQueryParameterAction(rule)
        val request = DebugRequest("https://www.example.com?token=123&type=auth")

        assertEquals("https://www.example.com?type=auth", action.apply(request).url)
    }

    @Test
    fun testRemoveNoMatchingValue() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.REMOVE_QUERY_PARAM, matchHeader = null, matchHeaderRegex = false,
                newValueRegex = false, newValue = null, matchValue = "1324", newHeader = null, caseSensitive = true, matchRequest = true,
                matchResponse = false, matchValueRegex = false, matchWholeValue = false, newHeaderRegex = false, replaceType = ReplaceType.REPLACE_ALL)

        val action = RemoveQueryParameterAction(rule)
        val request = DebugRequest("https://www.example.com?token=123&type=auth")

        assertEquals("https://www.example.com?token=123&type=auth", action.apply(request).url)
    }
}