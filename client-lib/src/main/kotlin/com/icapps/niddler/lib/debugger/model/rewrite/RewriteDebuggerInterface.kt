package com.icapps.niddler.lib.debugger.model.rewrite

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.DebuggerService
import org.apache.http.entity.ContentType
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
            debuggerService.removeRequestOverrideMethod(it.first)
            debuggerService.removeResponseAction(it.first)
        }
    }

    private fun createLocationInterceptor(rewriteLocationMatch: RewriteLocationMatch): Pair<String, String> {
        val active = rewriteLocationMatch.enabled
        val regex = rewriteLocationMatch.location.asRegex()
        val request = debuggerService.addRequestOverride(regex, method = null, active = active)
        val response = debuggerService.addResponseIntercept(regex, method = null, responseCode = null, active = active)

        return request to response
    }

}

class RewriteDebugListener : NiddlerDebugListener {

    private var rulesSets: List<RewriteSet> = emptyList()

    fun updateRuleSets(ruleSets: List<RewriteSet>) {
        this.rulesSets = ruleSets
    }

    override fun onRequestOverride(message: NiddlerMessage): DebugRequest? {
        val url = message.url ?: return null
        val method = message.method ?: return null

        var newRequest: DebugRequest? = null
        rulesSets.forEach { ruleSet ->
            if (!ruleSet.active) return@forEach
            if (ruleSet.matchesUrl(url)) {
                val activeRules = ruleSet.rules.filter { it.active && it.matchRequest }
                activeRules.forEach { rule ->
                    newRequest = applyRequestRule(rule, newRequest
                            ?: DebugRequest(url, method = method, headers = message.headers, encodedBody = message.body,
                                    bodyMimeType = message.headers?.get("content-type")?.firstOrNull()?.let { ContentType.parse(it).mimeType }))
                }
            }
        }
        return newRequest
    }

    override fun onRequestAction(requestId: String): DebugResponse? {
        return null
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage): DebugResponse? {
        return null
    }

    private fun applyRequestRule(rule: RewriteRule, modifyRequest: DebugRequest): DebugRequest {
        when (rule.ruleType) {
            RewriteType.ADD_HEADER -> {
                val newHeaderKey = rule.newHeader?.toLowerCase()
                val newHeaderValue = rule.newValue
                if (newHeaderKey.isNullOrBlank() || newHeaderValue.isNullOrBlank()) return modifyRequest
                val headers = modifyRequest.headers?.toMutableMap()?: mutableMapOf()
                if (!rule.matchHeader.isNullOrBlank()) {
                    val headerToMatch = (if (!rule.matchHeaderRegex)
                        headers[rule.matchHeader.toLowerCase()]
                    else
                        headers.entries.find { it.key.matches(Regex(rule.matchHeader)) }?.value)
                            ?: return modifyRequest

                    //TODO value matching
                }
                headers[newHeaderKey] = (headers[newHeaderKey]?.toMutableList() ?: mutableListOf()).also { it.add(newHeaderValue) }
                return DebugRequest(modifyRequest.url, modifyRequest.method, headers, modifyRequest.encodedBody, modifyRequest.bodyMimeType)
            }
            RewriteType.MODIFY_HEADER -> TODO()
            RewriteType.REMOVE_HEADER -> TODO()
            RewriteType.HOST -> TODO()
            RewriteType.PATH -> TODO()
            RewriteType.URL -> TODO()
            RewriteType.ADD_QUERY_PARAM -> TODO()
            RewriteType.MODIFY_QUERY_PARAM -> TODO()
            RewriteType.REMOVE_QUERY_PARAM -> TODO()
            RewriteType.RESPONSE_STATUS -> TODO()
            RewriteType.BODY -> TODO()
        }
    }

}

private data class RuleSetReference(val ruleSet: RewriteSet, val remoteIds: List<Pair<String, String>>)