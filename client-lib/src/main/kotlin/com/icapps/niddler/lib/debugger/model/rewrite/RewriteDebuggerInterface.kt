package com.icapps.niddler.lib.debugger.model.rewrite

import com.icapps.niddler.lib.debugger.model.DebuggerService
import java.util.UUID

class RewriteDebuggerInterface(private val debuggerService: DebuggerService) {

    private val rulesetMap = mutableMapOf<String, RuleSetReference>()

    fun addRuleSet(ruleSet: RewriteSet): String {
        val id = UUID.randomUUID().toString()
        val ids = ruleSet.locations.map(::createLocationInterceptor)

        rulesetMap[id] = RuleSetReference(ruleSet, ids)

        return id
    }

    fun removeRuleSet(token: String) {
        val reference = rulesetMap.remove(token) ?: return
        reference.remoteIds.forEach {
            debuggerService.removeRequestAction(it.first)
            debuggerService.removeResponseAction(it.first)
        }
    }

    private fun createLocationInterceptor(rewriteLocationMatch: RewriteLocationMatch): Pair<String, String> {
        val active = rewriteLocationMatch.enabled
        val regex = rewriteLocationMatch.location.asRegex()
        val request = debuggerService.addRequestIntercept(regex, method = null, active = active)
        val response = debuggerService.addResponseIntercept(regex, method = null, responseCode = null, active = active)

        return request to response
    }

}

private data class RuleSetReference(val ruleSet: RewriteSet, val remoteIds: List<Pair<String, String>>)