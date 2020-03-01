package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import org.junit.Assert.assertEquals
import org.junit.Test

class ModifyBodyActionTest {

    private val request = DebugRequest("https://www.exampleNL.com/api/NL/NL/upload.json", "POST", headers = null,
            bodyMimeType = "application/json", encodedBody = "eyAiZGF0YSI6IHsgIm9wdGlvbnMiOiB7fSwgImludGVybmFsIjogImRvbid0IGNoYW5nZSIgfSB9")

    private val response = DebugResponse(200, "OK", headers = null,
            bodyMimeType = "application/json", encodedBody = "eyAiZGF0YSI6IHsgIm9wdGlvbnMiOiB7fSwgImludGVybmFsIjogImRvbid0IGNoYW5nZSIgfSB9")

    @Test
    fun testReplaceRequestInside() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.BODY, matchValue = "\"options\": {}", matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "\"options\": {\"max_items\":1}", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyBodyAction(rule).apply(request)
        assertEquals("eyAiZGF0YSI6IHsgIm9wdGlvbnMiOiB7Im1heF9pdGVtcyI6MX0sICJpbnRlcm5hbCI6ICJkb24ndCBjaGFuZ2UiIH0gfQ", request.encodedBody)
    }

    @Test
    fun testReplaceRequestFull() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.BODY, matchValue = null, matchHeader = null, matchRequest = true, matchResponse = false,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "\"options\": {\"max_items\":1}", newValueRegex = false, matchHeaderRegex = false)

        val request = ModifyBodyAction(rule).apply(request)
        assertEquals("Im9wdGlvbnMiOiB7Im1heF9pdGVtcyI6MX0", request.encodedBody)
    }

    @Test
    fun testReplaceResponseInside() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.BODY, matchValue = "\"options\": {}", matchHeader = null, matchRequest = true, matchResponse = true,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "\"options\": {\"max_items\":1}", newValueRegex = false, matchHeaderRegex = false)

        val response = ModifyBodyAction(rule).apply(response)
        assertEquals("eyAiZGF0YSI6IHsgIm9wdGlvbnMiOiB7Im1heF9pdGVtcyI6MX0sICJpbnRlcm5hbCI6ICJkb24ndCBjaGFuZ2UiIH0gfQ", response.encodedBody)
    }

    @Test
    fun testReplaceResponseFull() {
        val rule = RewriteRule(active = true, ruleType = RewriteType.BODY, matchValue = null, matchHeader = null, matchRequest = true, matchResponse = true,
                replaceType = ReplaceType.REPLACE_FIRST, newHeaderRegex = false, matchWholeValue = false, matchValueRegex = false, caseSensitive = false, newHeader = null,
                newValue = "\"options\": {\"max_items\":1}", newValueRegex = false, matchHeaderRegex = false)

        val response = ModifyBodyAction(rule).apply(response)
        assertEquals("Im9wdGlvbnMiOiB7Im1heF9pdGVtcyI6MX0", response.encodedBody)
    }
}