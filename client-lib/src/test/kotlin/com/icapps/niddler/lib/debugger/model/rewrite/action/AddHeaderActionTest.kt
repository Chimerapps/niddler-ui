package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AddHeaderActionTest {

    private lateinit var responseRule: RewriteRule
    private lateinit var requestRule: RewriteRule
    private lateinit var combinedRule: RewriteRule

    private lateinit var responseAction: AddHeaderAction
    private lateinit var requestAction: AddHeaderAction
    private lateinit var combinedAction: AddHeaderAction

    private lateinit var requestHeaders: Map<String, List<String>>
    private lateinit var request: DebugRequest
    private lateinit var response: DebugResponse

    @Before
    fun setUp() {
        responseRule = RewriteRule(active = true, ruleType = RewriteType.ADD_HEADER, matchHeader = "X-Token", matchHeaderRegex = false,
                newValueRegex = false, newValue = "Added", matchValue = null, newHeader = "X-Test", caseSensitive = true, matchRequest = false,
                matchResponse = true, matchValueRegex = false, matchWholeValue = false, newHeaderRegex = false, replaceType = ReplaceType.REPLACE_ALL)
        requestRule = responseRule.copy(matchResponse = false, matchRequest = true)
        combinedRule = responseRule.copy(matchResponse = true, matchRequest = true)

        responseAction = AddHeaderAction(responseRule)
        requestAction = AddHeaderAction(requestRule)
        combinedAction = AddHeaderAction(combinedRule)

        requestHeaders = mapOf("Client-Id" to listOf("992-11"), "x-token" to listOf("x-test-token"))
        request = DebugRequest("http://www.example.com", "GET", requestHeaders, null, null)
        response = DebugResponse(200, "OK", requestHeaders, null, null)
    }

    @Test
    fun testApplyResponse() {
        assertEquals("Added", responseAction.apply(response).headers?.get("x-test")?.first())
        assertEquals("Added", combinedAction.apply(response).headers?.get("x-test")?.first())
        assertNull(requestAction.apply(response).headers?.get("x-test")?.first())
    }

    @Test
    fun testApplyRequest() {
        assertNull(responseAction.apply(request).headers?.get("x-test")?.first())
        assertEquals("Added", combinedAction.apply(request).headers?.get("x-test")?.first())
        assertEquals("Added", requestAction.apply(request).headers?.get("x-test")?.first())
    }
}