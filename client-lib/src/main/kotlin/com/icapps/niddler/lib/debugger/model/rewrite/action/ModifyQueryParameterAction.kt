package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import com.icapps.niddler.lib.utils.UrlUtil

//TODO test
class ModifyQueryParameterAction(rule: RewriteRule) : BaseModifyQueryParameterAction(rule), BaseModifyParameterAction {

    init {
        if (rule.ruleType != RewriteType.MODIFY_QUERY_PARAM) throw IllegalArgumentException("Not modify query parameter type")
    }

    fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        val queryValues = UrlUtil(debugRequest.url).query

        val newUrl = updateUrl(debugRequest.url, modifyMap(rule, matches(queryValues), queryValues) ?: emptyMap())

        return DebugRequest(newUrl, debugRequest.method, debugRequest.headers, debugRequest.encodedBody, debugRequest.bodyMimeType)
    }

}