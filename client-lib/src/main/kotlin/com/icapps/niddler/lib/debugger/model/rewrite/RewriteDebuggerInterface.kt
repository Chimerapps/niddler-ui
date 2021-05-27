package com.icapps.niddler.lib.debugger.model.rewrite

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.DebuggerService
import com.icapps.niddler.lib.debugger.model.rewrite.action.AddHeaderAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.AddQueryParameterAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ModifyBodyAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ModifyHeaderAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ModifyHostAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ModifyPathAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ModifyQueryParameterAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ModifyStatusAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ModifyUrlAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.RemoveHeaderAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.RemoveQueryParameterAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.RequestAction
import com.icapps.niddler.lib.debugger.model.rewrite.action.ResponseAction
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

    fun clearRuleSets() {
        rulesetMap.forEach { (_, ruleSetReference) ->
            ruleSetReference.remoteIds.forEach {
                debuggerService.removeRequestOverrideMethod(it.first)
                debuggerService.removeResponseAction(it.first)
            }
        }
    }

}

class RewriteDebugListener(private val onWrongStatusMessageReplacement: (String) -> Unit) : NiddlerDebugListener {

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
                val activeActions = ruleSet.rules.mapNotNull { if (it.active && it.matchRequest) makeRequestAction(it) else null }
                activeActions.forEach { action ->
                    newRequest = action.apply(newRequest
                            ?: DebugRequest(url, method = method, headers = message.headers, encodedBody = message.body,
                                    bodyMimeType = message.headers?.get("content-type")?.firstOrNull()?.let { ContentType.parse(it).mimeType }))
                }
            }
        }
        return newRequest
    }

    override fun onRequestAction(requestId: String, request: NiddlerMessage?): DebugResponse? {
        return null
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        val url = request?.url ?: return null

        var newResponse: DebugResponse? = null
        rulesSets.forEach { ruleSet ->
            if (!ruleSet.active) return@forEach
            if (ruleSet.matchesUrl(url)) {
                val activeActions = ruleSet.rules.mapNotNull { if (it.active && it.matchResponse) makeResponseAction(it) else null }
                activeActions.forEach { action ->
                    newResponse = action.apply(newResponse
                            ?: DebugResponse(response.statusCode ?: 200, response.statusLine ?: "OK", headers = response.headers, encodedBody = response.body,
                                    bodyMimeType = response.headers?.get("content-type")?.firstOrNull()?.let { ContentType.parse(it).mimeType }))
                }
            }
        }
        return newResponse
    }

    private fun makeRequestAction(rule: RewriteRule): RequestAction? {
        return when (rule.ruleType) {
            RewriteType.ADD_HEADER -> AddHeaderAction(rule)
            RewriteType.MODIFY_HEADER -> ModifyHeaderAction(rule)
            RewriteType.REMOVE_HEADER -> RemoveHeaderAction(rule)
            RewriteType.HOST -> ModifyHostAction(rule)
            RewriteType.PATH -> ModifyPathAction(rule)
            RewriteType.URL -> ModifyUrlAction(rule)
            RewriteType.ADD_QUERY_PARAM -> AddQueryParameterAction(rule)
            RewriteType.MODIFY_QUERY_PARAM -> ModifyQueryParameterAction(rule)
            RewriteType.REMOVE_QUERY_PARAM -> RemoveQueryParameterAction(rule)
            RewriteType.BODY -> ModifyBodyAction(rule)
            RewriteType.RESPONSE_STATUS -> null
        }
    }

    private fun makeResponseAction(rule: RewriteRule): ResponseAction? {
        return when (rule.ruleType) {
            RewriteType.ADD_HEADER -> AddHeaderAction(rule)
            RewriteType.MODIFY_HEADER -> ModifyHeaderAction(rule)
            RewriteType.REMOVE_HEADER -> RemoveHeaderAction(rule)
            RewriteType.BODY -> ModifyBodyAction(rule)
            RewriteType.RESPONSE_STATUS -> ModifyStatusAction(rule, onWrongStatusMessageReplacement)
            RewriteType.HOST,
            RewriteType.PATH,
            RewriteType.URL,
            RewriteType.ADD_QUERY_PARAM,
            RewriteType.MODIFY_QUERY_PARAM,
            RewriteType.REMOVE_QUERY_PARAM -> null
        }
    }

}

private data class RuleSetReference(val ruleSet: RewriteSet, val remoteIds: List<Pair<String, String>>)