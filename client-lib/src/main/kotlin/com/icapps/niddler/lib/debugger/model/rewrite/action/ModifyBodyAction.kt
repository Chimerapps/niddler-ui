package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import java.util.Base64

class ModifyBodyAction(rule: RewriteRule) : BaseValueMatcher(rule) {
    init {
        if (rule.ruleType != RewriteType.BODY) throw IllegalArgumentException("Rule is not a modify body rule")
    }

    fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest
        val newBody = createReplacement(decodeBodyString(debugRequest.bodyMimeType, debugRequest.encodedBody))
        return debugRequest.copy(encodedBody = Base64.getUrlEncoder().withoutPadding().encodeToString(newBody.toByteArray(Charsets.UTF_8)))
    }

    fun apply(debugResponse: DebugResponse): DebugResponse {
        if (!rule.matchResponse) return debugResponse

        val newBody = createReplacement(decodeBodyString(debugResponse.bodyMimeType, debugResponse.encodedBody))
        return debugResponse.copy(encodedBody = Base64.getUrlEncoder().withoutPadding().encodeToString(newBody.toByteArray(Charsets.UTF_8)))
    }

    private fun decodeBodyString(bodyMimeType: String?, encodedBody: String?): String {
        return String(encodedBody?.let { Base64.getUrlDecoder().decode(encodedBody) } ?: return "")
    }
}