package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import org.junit.Assert.assertEquals
import org.junit.Test

class ModifyStatusActionTest {

    private val response = DebugResponse(200, "OK", null, null, null)

    @Test
    fun testNormal() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.RESPONSE_STATUS, matchValue = "200 OK", matchHeader = null, matchRequest = false, matchResponse = true,
                replaceType = ReplaceType.REPLACE_ALL, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "400 Bad request", newValueRegex = false, matchHeaderRegex = false)

        val response = ModifyStatusAction(rule).apply(response)
        assertEquals(400, response.code)
        assertEquals("Bad request", response.message)
    }

    @Test
    fun testRegex() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.RESPONSE_STATUS, matchValue = "(.*) OK", matchHeader = null, matchRequest = false, matchResponse = true,
                replaceType = ReplaceType.REPLACE_ALL, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = true, caseSensitive = false, newHeader = null,
                newValue = "$1 Bad request", newValueRegex = false, matchHeaderRegex = false)

        val response = ModifyStatusAction(rule).apply(response)
        assertEquals(200, response.code)
        assertEquals("Bad request", response.message)
    }
}