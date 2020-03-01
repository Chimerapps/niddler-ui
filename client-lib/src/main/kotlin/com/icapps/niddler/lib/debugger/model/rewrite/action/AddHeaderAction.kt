package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType

class AddHeaderAction(rule: RewriteRule) : BaseModifyMapAction(rule) {

    init {
        if (rule.ruleType != RewriteType.ADD_HEADER) throw IllegalArgumentException("Not add header type")
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
        val newHeader = rule.newHeader?.toLowerCase()
        val newValue = rule.newValue
        if (newHeader.isNullOrBlank() || newValue.isNullOrBlank())
            return originalHeaders

        val match = matches(originalHeaders)
        if (!match.matches) return originalHeaders

        val mutableHeaders = originalHeaders?.toMutableMap() ?: mutableMapOf()

        mutableHeaders[newHeader] = if (rule.replaceType == ReplaceType.REPLACE_ALL)
            listOf(newValue)
        else
            (mutableHeaders[newHeader]?.toMutableList() ?: mutableListOf()).also { it.add(newValue) }
        return mutableHeaders
    }
}