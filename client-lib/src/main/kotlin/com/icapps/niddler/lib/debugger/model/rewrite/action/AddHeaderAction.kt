package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType

class AddHeaderAction(rule: RewriteRule) : BaseModifyMapAction(rule), BaseAddParameterAction, RequestAction, ResponseAction {

    init {
        if (rule.ruleType != RewriteType.ADD_HEADER) throw IllegalArgumentException("Not add header type")
    }

    override fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        return DebugRequest(debugRequest.url, debugRequest.method, apply(debugRequest.headers), debugRequest.encodedBody, debugRequest.bodyMimeType)
    }

    override fun apply(debugResponse: DebugResponse): DebugResponse {
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

        return modifyMap(rule, newHeader, newValue, match, originalHeaders)
    }
}