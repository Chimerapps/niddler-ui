package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import org.junit.Assert.assertEquals
import org.junit.Test

class ModifyPathActionTest {

    private val request = DebugRequest("https://www.exampleNL.com/api/NL/NL/upload.json", "GET")

    @Test
    fun testNormalFirst() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.PATH, matchValue = "NL", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "EN", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyPathAction(rule).apply(request)
        assertEquals("https://www.exampleNL.com/api/EN/NL/upload.json", request.url)
    }

    @Test
    fun testNormalAll() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.PATH, matchValue = "NL", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_ALL, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "EN", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyPathAction(rule).apply(request)
        assertEquals("https://www.exampleNL.com/api/EN/EN/upload.json", request.url)
    }

    @Test
    fun testNormalAllComplex() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.PATH, matchValue = "NL/NL", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_ALL, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "EN", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyPathAction(rule).apply(request)
        assertEquals("https://www.exampleNL.com/api/EN/upload.json", request.url)
    }

    @Test
    fun testRegexAll() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.PATH, matchValue = "(.*).json", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_ALL, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = true, caseSensitive = false, newHeader = null,
                newValue = "$1.test.json", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyPathAction(rule).apply(request)
        assertEquals("https://www.exampleNL.com/api/NL/NL/upload.test.json", request.url)
    }

    @Test
    fun testRegexFirst() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.PATH, matchValue = "(.*).json", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = true, caseSensitive = false, newHeader = null,
                newValue = "$1.test.json", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyPathAction(rule).apply(request)
        assertEquals("https://www.exampleNL.com/api/NL/NL/upload.test.json", request.url)
    }

}