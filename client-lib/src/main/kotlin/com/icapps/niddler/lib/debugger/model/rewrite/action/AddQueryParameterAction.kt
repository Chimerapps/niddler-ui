package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import com.icapps.niddler.lib.utils.UrlUtil

class AddQueryParameterAction(rule: RewriteRule) : BaseModifyQueryParameterAction(rule) {

    init {
        if (rule.ruleType != RewriteType.ADD_QUERY_PARAM) throw IllegalArgumentException("Not add query parameter type")
    }

    fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        val newHeader = rule.newHeader?.toLowerCase()
        val newValue = rule.newValue
        if (newHeader.isNullOrBlank() || newValue.isNullOrBlank())
            return debugRequest

        val queryValues = UrlUtil(debugRequest.url).query

        val match = matches(queryValues)
        if (!match.matches) return debugRequest

        val mutableQueries = queryValues.toMutableMap()
        mutableQueries[newHeader] = if (rule.replaceType == ReplaceType.REPLACE_ALL)
            listOf(newValue)
        else
            (mutableQueries[newHeader]?.toMutableList() ?: mutableListOf()).also { it.add(newValue) }

        val newUrl = updateUrl(debugRequest.url, mutableQueries)

        return DebugRequest(newUrl, debugRequest.method, debugRequest.headers, debugRequest.encodedBody, debugRequest.bodyMimeType)
    }

}