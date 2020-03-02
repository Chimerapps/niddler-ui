package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.utils.UrlUtil

class RemoveQueryParameterAction(rule: RewriteRule) : BaseModifyQueryParameterAction(rule), RequestAction {

    override fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        val queryValues = UrlUtil(debugRequest.url).query
        if (queryValues.isEmpty()) return debugRequest

        val match = matches(queryValues)
        if (!match.matches) return debugRequest
        val matchedHeader = match.matchedHeader ?: return debugRequest

        val newQueryValues = queryValues.toMutableMap().also { it.remove(matchedHeader) }

        val newUrl = updateUrl(debugRequest.url, newQueryValues)

        return DebugRequest(newUrl, debugRequest.method, debugRequest.headers, debugRequest.encodedBody, debugRequest.bodyMimeType)
    }

}