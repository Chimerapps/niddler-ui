package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import com.icapps.niddler.lib.utils.newBuilder
import java.net.URL

class ModifyHostAction(rule: RewriteRule) : BaseValueMatcher(rule) {
    init {
        if (rule.ruleType != RewriteType.HOST) throw IllegalArgumentException("Rule is not a modify host rule")
    }

    fun apply(debugRequest: DebugRequest): DebugRequest {
        val url = try {
            URL(debugRequest.url)
        } catch (e: Throwable) {
            return debugRequest
        }

        val newHost = createReplacement(url.host)
        return debugRequest.copy(url = url.newBuilder().also { it.host = newHost }.toString())
    }

}