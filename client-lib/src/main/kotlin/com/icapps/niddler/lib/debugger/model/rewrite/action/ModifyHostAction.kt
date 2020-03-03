package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import com.icapps.niddler.lib.utils.newBuilder
import java.net.URI

class ModifyHostAction(rule: RewriteRule) : BaseValueMatcher(rule), RequestAction {
    init {
        if (rule.ruleType != RewriteType.HOST) throw IllegalArgumentException("Rule is not a modify host rule")
    }

    @Suppress("DuplicatedCode")
    override fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        val url = try {
            URI(debugRequest.url)
        } catch (e: Throwable) {
            return debugRequest
        }

        val newHost = createReplacement(url.host)
        return debugRequest.copy(url = url.newBuilder().also { it.host = newHost }.build().toString())
    }

}