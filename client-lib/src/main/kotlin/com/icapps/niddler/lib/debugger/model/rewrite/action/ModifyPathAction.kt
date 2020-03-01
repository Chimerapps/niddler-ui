package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import com.icapps.niddler.lib.utils.newBuilder
import java.net.URI

class ModifyPathAction(rule: RewriteRule) : BaseValueMatcher(rule) {

    init {
        if (rule.ruleType != RewriteType.PATH) throw IllegalArgumentException("Rule is not a modify path rule")
    }

    @Suppress("DuplicatedCode")
    fun apply(debugRequest: DebugRequest): DebugRequest {
        if (!rule.matchRequest) return debugRequest

        val url = try {
            URI(debugRequest.url)
        } catch (e: Throwable) {
            return debugRequest
        }

        val newHost = createReplacement(url.path)
        return debugRequest.copy(url = url.newBuilder().also { it.path = newHost }.build().toString())
    }

}