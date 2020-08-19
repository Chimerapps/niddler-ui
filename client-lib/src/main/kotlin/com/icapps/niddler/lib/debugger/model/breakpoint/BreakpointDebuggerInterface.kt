package com.icapps.niddler.lib.debugger.model.breakpoint

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.DebuggerService
import com.icapps.niddler.lib.debugger.model.configuration.DebuggerLocationMatch
import org.apache.http.entity.ContentType
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class BreakpointDebuggerInterface(private val debuggerService: DebuggerService) {

    private val breakpointMap = mutableMapOf<String, BreakpointReference>()

    fun addBreakpoint(breakpoint: Breakpoint): String {
        val id = UUID.randomUUID().toString()
        val ids = breakpoint.locations.map { createLocationInterceptor(it, breakpoint.method) }

        synchronized(breakpointMap) { breakpointMap[id] = BreakpointReference(breakpoint, ids) }

        return id
    }

    fun removeBreakpoint(token: String) {
        val reference = synchronized(breakpointMap) { breakpointMap.remove(token) } ?: return
        reference.remoteIds.forEach {
            debuggerService.removeRequestOverrideMethod(it.first)
            debuggerService.removeResponseAction(it.first)
        }
    }

    private fun createLocationInterceptor(debuggerLocationMatch: DebuggerLocationMatch, method: String?): Pair<String, String> {
        val active = debuggerLocationMatch.enabled
        val regex = debuggerLocationMatch.location.asRegex()
        val request = debuggerService.addRequestOverride(regex, method = method, active = active)
        val response = debuggerService.addResponseIntercept(regex, method = method, responseCode = null, active = active)

        return request to response
    }

    fun clearBreakpoints() {
        synchronized(breakpointMap) {
            breakpointMap.forEach { (_, ruleSetReference) ->
                ruleSetReference.remoteIds.forEach {
                    debuggerService.removeRequestOverrideMethod(it.first)
                    debuggerService.removeResponseAction(it.first)
                }
            }
            breakpointMap.clear()
        }
    }

    fun isRegistered(id: String): Boolean = id.isEmpty() || synchronized(breakpointMap) { breakpointMap.containsKey(id) } //Is empty is for legacy clients

}

interface DebugActionHandler {
    fun handleDebugRequest(request: DebugRequest): DebugRequest
    fun handleDebugResponse(request: DebugResponse): DebugResponse
}

class BreakpointDebugListener(private val actionHandler: DebugActionHandler,
                              private val activeCountChangedListener: () -> Unit) : NiddlerDebugListener {

    private var breakpoints: List<Breakpoint> = emptyList()
    private var debuggerInterface: BreakpointDebuggerInterface?=null
    private var numOverridesActive = AtomicInteger()

    val hasActiveBreakpoint: Boolean
        get() = numOverridesActive.get() > 0

    fun updateBreakpoints(breakpoints: List<Breakpoint>, debuggerInterface: BreakpointDebuggerInterface) {
        this.breakpoints = breakpoints
        this.debuggerInterface = debuggerInterface
    }

    override fun onRequestOverride(actionId: String, message: NiddlerMessage): DebugRequest? {
        if (debuggerInterface?.isRegistered(actionId) != true) return null

        val url = message.url ?: return null
        val method = message.method ?: return null

        try {
            breakpoints.forEach { breakpoint ->
                if (!breakpoint.active) return@forEach
                if (breakpoint.matchesUrl(url) && breakpoint.method != null && breakpoint.method.equals(message.method, true)) {
                    numOverridesActive.incrementAndGet()
                    activeCountChangedListener()

                    val request = DebugRequest(url, method = method, headers = message.headers, encodedBody = message.body,
                            bodyMimeType = message.headers?.get("content-type")?.firstOrNull()?.let { ContentType.parse(it).mimeType })
                    return actionHandler.handleDebugRequest(request)
                }
            }
        } finally {
            numOverridesActive.decrementAndGet()
            activeCountChangedListener()
        }
        return null
    }

    override fun onRequestAction(actionId: String, requestId: String): DebugResponse? {
        return null
    }

    override fun onResponseAction(actionId: String, requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        if (debuggerInterface?.isRegistered(actionId) != true) return null

        val url = request?.url ?: return null

        try {
            breakpoints.forEach { breakpoint ->
                if (!breakpoint.active) return@forEach
                if (breakpoint.matchesUrl(url) && breakpoint.method != null && breakpoint.method.equals(request.method, true)) {
                    numOverridesActive.incrementAndGet()
                    activeCountChangedListener()

                    val debugResponse = DebugResponse(response.statusCode ?: 200, response.statusLine ?: "OK", headers = response.headers, encodedBody = response.body,
                            bodyMimeType = response.headers?.get("content-type")?.firstOrNull()?.let { ContentType.parse(it).mimeType })
                    return actionHandler.handleDebugResponse(debugResponse)
                }
            }
        } finally {
            numOverridesActive.decrementAndGet()
            activeCountChangedListener()
        }
        return null
    }

}

private data class BreakpointReference(val breakpoint: Breakpoint, val remoteIds: List<Pair<String, String>>)