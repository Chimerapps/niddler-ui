package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType

//TODO test
class ModifyHeaderAction(rule: RewriteRule) : BaseModifyMapAction(rule) {

    init {
        if (rule.ruleType != RewriteType.MODIFY_HEADER) throw IllegalArgumentException("Not modify header type")
    }

    fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        return DebugRequest(debugRequest.url, debugRequest.method, apply(debugRequest.headers), debugRequest.encodedBody, debugRequest.bodyMimeType)
    }

    fun apply(debugResponse: DebugResponse): DebugResponse {
        if (!rule.matchResponse) return debugResponse

        return DebugResponse(debugResponse.code, debugResponse.message, apply(debugResponse.headers), debugResponse.encodedBody, debugResponse.bodyMimeType)
    }

    //TODO regex

    private fun apply(originalHeaders: Map<String, List<String>>?): Map<String, List<String>>? {
        val match = matches(originalHeaders)
        if (!match.matches) return originalHeaders

        var newHeader = rule.newHeader?.toLowerCase()

        val matchedHeader = match.matchedHeader ?: newHeader ?: return originalHeaders
        if (newHeader == null) newHeader = matchedHeader

        val matchedValue = match.matchedValue ?: rule.newValue ?: return originalHeaders
        if (originalHeaders == null) {
            return mapOf(matchedHeader to listOf(matchedValue))
        }

        val mutableHeaders = originalHeaders.toMutableMap()
        if (matchedHeader != newHeader) {
            mutableHeaders[newHeader] = mutableHeaders.remove(matchedHeader) ?: return originalHeaders
        }

        val originalValues = mutableHeaders[newHeader]?.toMutableList() ?: mutableListOf()

        if (rule.replaceType == ReplaceType.REPLACE_ALL) {
            if (rule.newValue != null) {
                originalValues.clear()
                originalValues.add(rule.newValue)
            }
        } else if (rule.newValue != null) {
            originalValues.remove(matchedValue)
            originalValues.add(rule.newValue)
        }
        mutableHeaders[newHeader] = originalValues

        return mutableHeaders
    }
}