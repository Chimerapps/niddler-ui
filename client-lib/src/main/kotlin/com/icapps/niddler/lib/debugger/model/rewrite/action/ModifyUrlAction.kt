package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType

class ModifyUrlAction(rule: RewriteRule) : BaseValueMatcher(rule), RequestAction {

    init {
        if (rule.ruleType != RewriteType.URL) throw IllegalArgumentException("Rule is not a modify url rule")
    }

    override fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        val newUrl = createReplacement(debugRequest.url)
        return debugRequest.copy(url = newUrl)
    }

}