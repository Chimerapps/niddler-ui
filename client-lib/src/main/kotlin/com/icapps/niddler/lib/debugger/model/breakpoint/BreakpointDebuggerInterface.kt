package com.icapps.niddler.lib.debugger.model.breakpoint

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.DebuggerService
import com.icapps.niddler.lib.debugger.model.configuration.DebuggerLocationMatch
import org.apache.http.entity.ContentType
import java.util.UUID

class BreakpointDebuggerInterface(private val debuggerService: DebuggerService) {

    private val breakpointMap = mutableMapOf<String, BreakpointReference>()

    fun addBreakpoint(breakpoint: Breakpoint): String {
        val id = UUID.randomUUID().toString()
        val ids = breakpoint.locations.map(::createLocationInterceptor)

        breakpointMap[id] = BreakpointReference(breakpoint, ids)

        return id
    }

    fun removeBreakpoint(token: String) {
        val reference = breakpointMap.remove(token) ?: return
        reference.remoteIds.forEach {
            debuggerService.removeRequestOverrideMethod(it.first)
            debuggerService.removeResponseAction(it.first)
        }
    }

    private fun createLocationInterceptor(debuggerLocationMatch: DebuggerLocationMatch): Pair<String, String> {
        val active = debuggerLocationMatch.enabled
        val regex = debuggerLocationMatch.location.asRegex()
        val request = debuggerService.addRequestOverride(regex, method = null, active = active)
        val response = debuggerService.addResponseIntercept(regex, method = null, responseCode = null, active = active)

        return request to response
    }

    fun clearBreakpoints() {
        breakpointMap.forEach { (_, ruleSetReference) ->
            ruleSetReference.remoteIds.forEach {
                debuggerService.removeRequestOverrideMethod(it.first)
                debuggerService.removeResponseAction(it.first)
            }
        }
    }

}

interface DebugActionHandler {
    fun handleDebugRequest(request: DebugRequest): DebugRequest
    fun handleDebugResponse(request: DebugResponse): DebugResponse
}

class BreakpointDebugListener(private val actionHandler: DebugActionHandler) : NiddlerDebugListener {

    private var breakpoints: List<Breakpoint> = emptyList()

    fun updateBreakpoints(breakpoints: List<Breakpoint>) {
        this.breakpoints = breakpoints
    }

    override fun onRequestOverride(message: NiddlerMessage): DebugRequest? {
        val url = message.url ?: return null
        val method = message.method ?: return null

        breakpoints.forEach { breakpoint ->
            if (!breakpoint.active) return@forEach
            if (breakpoint.matchesUrl(url)) {
                val request = DebugRequest(url, method = method, headers = message.headers, encodedBody = message.body,
                        bodyMimeType = message.headers?.get("content-type")?.firstOrNull()?.let { ContentType.parse(it).mimeType })
                return actionHandler.handleDebugRequest(request)
            }
        }
        return null
    }

    override fun onRequestAction(requestId: String): DebugResponse? {
        return null
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        val url = request?.url ?: return null

        breakpoints.forEach { breakpoint ->
            if (!breakpoint.active) return@forEach
            if (breakpoint.matchesUrl(url)) {
                val debugResponse = DebugResponse(response.statusCode ?: 200, response.statusLine ?: "OK", headers = response.headers, encodedBody = response.body,
                        bodyMimeType = response.headers?.get("content-type")?.firstOrNull()?.let { ContentType.parse(it).mimeType })
                return actionHandler.handleDebugResponse(debugResponse)
            }
        }
        return null
    }

}

private data class BreakpointReference(val breakpoint: Breakpoint, val remoteIds: List<Pair<String, String>>)