package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RemoveHeaderActionTest {
    private lateinit var responseRuleName: RewriteRule
    private lateinit var requestRuleName: RewriteRule
    private lateinit var combinedRuleName: RewriteRule
    private lateinit var responseRuleValue: RewriteRule
    private lateinit var requestRuleValue: RewriteRule
    private lateinit var combinedRuleValue: RewriteRule
    private lateinit var responseRuleNameAndValue: RewriteRule
    private lateinit var requestRuleNameAndValue: RewriteRule
    private lateinit var combinedRuleNameAndValue: RewriteRule

    private lateinit var requestHeaders: Map<String, List<String>>
    private lateinit var request: DebugRequest
    private lateinit var response: DebugResponse

    @Before
    fun setUp() {
        responseRuleName = RewriteRule(active = true, ruleType = RewriteType.REMOVE_HEADER, matchHeader = "X-Token", matchHeaderRegex = false,
                newValueRegex = false, newValue = null, matchValue = null, newHeader = null, caseSensitive = true, matchRequest = false,
                matchResponse = true, matchValueRegex = false, matchWholeValue = false, newHeaderRegex = false, replaceType = ReplaceType.REPLACE_ALL)
        requestRuleName = responseRuleName.copy(matchResponse = false, matchRequest = true)
        combinedRuleName = responseRuleName.copy(matchResponse = true, matchRequest = true)

        responseRuleValue = responseRuleName.copy(matchHeader = null, matchValue = "x-test")
        requestRuleValue = responseRuleValue.copy(matchResponse = false, matchRequest = true)
        combinedRuleValue = responseRuleValue.copy(matchResponse = true, matchRequest = true)

        responseRuleNameAndValue = responseRuleValue.copy(matchHeader = "X-Token", matchValue = "x-test")
        requestRuleNameAndValue = responseRuleNameAndValue.copy(matchResponse = false, matchRequest = true)
        combinedRuleNameAndValue = responseRuleNameAndValue.copy(matchResponse = true, matchRequest = true)

        requestHeaders = mapOf("Client-Id" to listOf("992-11"), "x-token" to listOf("x-test-token"))
        request = DebugRequest("http://www.example.com", "GET", requestHeaders, null, null)
        response = DebugResponse(200, "OK", requestHeaders, null, null)
    }

    @Test
    fun testApplyResponseMatchName() {
        assertNull(RemoveHeaderAction(responseRuleName).apply(response).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(combinedRuleName).apply(response).headers?.get("x-token")?.first())
        assertNotNull(RemoveHeaderAction(requestRuleName).apply(response).headers?.get("x-token")?.first())
    }

    @Test
    fun testApplyRequestMatchName() {
        assertNotNull(RemoveHeaderAction(responseRuleName).apply(request).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(combinedRuleName).apply(request).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(requestRuleName).apply(request).headers?.get("x-token")?.first())
    }

    @Test
    fun testApplyResponseMatchValue() {
        assertNull(RemoveHeaderAction(responseRuleValue).apply(response).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(combinedRuleValue).apply(response).headers?.get("x-token")?.first())
        assertNotNull(RemoveHeaderAction(requestRuleValue).apply(response).headers?.get("x-token")?.first())
    }

    @Test
    fun testApplyRequestMatchValue() {
        assertNotNull(RemoveHeaderAction(responseRuleValue).apply(request).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(combinedRuleValue).apply(request).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(requestRuleValue).apply(request).headers?.get("x-token")?.first())
    }

    @Test
    fun testApplyResponseMatchNameAndValue() {
        assertNull(RemoveHeaderAction(responseRuleNameAndValue).apply(response).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(combinedRuleNameAndValue).apply(response).headers?.get("x-token")?.first())
        assertNotNull(RemoveHeaderAction(requestRuleNameAndValue).apply(response).headers?.get("x-token")?.first())
    }

    @Test
    fun testApplyRequestMatchNameAndValue() {
        assertNotNull(RemoveHeaderAction(responseRuleNameAndValue).apply(request).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(combinedRuleNameAndValue).apply(request).headers?.get("x-token")?.first())
        assertNull(RemoveHeaderAction(requestRuleNameAndValue).apply(request).headers?.get("x-token")?.first())
    }
}