package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType

class RemoveHeaderAction(rule: RewriteRule) : BaseModifyMapAction(rule) {

    init {
        if (rule.ruleType != RewriteType.REMOVE_HEADER) throw IllegalArgumentException("Not remove header type")
    }

    fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        return DebugRequest(debugRequest.url, debugRequest.method, apply(debugRequest.headers), debugRequest.encodedBody, debugRequest.bodyMimeType)
    }

    fun apply(debugResponse: DebugResponse): DebugResponse {
        if (!rule.matchResponse) return debugResponse

        return DebugResponse(debugResponse.code, debugResponse.message, apply(debugResponse.headers), debugResponse.encodedBody, debugResponse.bodyMimeType)
    }

    private fun apply(originalHeaders: Map<String, List<String>>?): Map<String, List<String>>? {
        if (originalHeaders == null || originalHeaders.isEmpty()) return originalHeaders

        val match = matches(originalHeaders)
        if (!match.matches) return originalHeaders

        val matchedHeader = match.matchedHeader ?: return originalHeaders

        val mutableHeaders = originalHeaders.toMutableMap()
        mutableHeaders.remove(matchedHeader)
        return mutableHeaders
    }
}