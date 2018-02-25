package com.icapps.niddler.ui.debugger.model

import com.google.gson.annotations.Expose

/**
 * @author nicolaverbeeck
 */
interface DebuggerInterface {

    fun activate()

    fun deactivate()

    fun updateBlacklist(active: Iterable<String>)

    fun updateDefaultResponses(items: Iterable<DefaultResponseAction>)

    fun mute()

    fun unmute()

    fun updateDelays(delays: DebuggerDelays?)

    fun debugDelays(): DebuggerDelays?

    fun connect()

    fun disconnect()
}

data class DefaultResponseAction(var id: String?,
                                 var enabled: Boolean,
                                 @Expose val regex: String?,
                                 @Expose val method: String?,
                                 @Expose val response: DebugResponse)

data class RequestOverride(@Expose var id: String = "",
                           @Expose var active: Boolean = false,
                           @Expose var regex: String? = null,
                           @Expose var matchMethod: String? = null,
                           @Expose var repeatCount: Int? = null,
                           var debugRequest: DebugRequest? = null) {
    override fun toString(): String {
        return regex ?: matchMethod ?: ""
    }
}

data class ResponseOverride(@Expose var id: String = "",
                            @Expose var active: Boolean = false,
                            @Expose var regex: String? = null,
                            @Expose var matchMethod: String? = null,
                            @Expose var repeatCount: Int? = null,
                            var debugResponse: DebugResponse? = null)

data class RequestIntercept(@Expose var id: String = "",
                            @Expose var active: Boolean = false,
                            @Expose var regex: String? = null,
                            @Expose var matchMethod: String? = null,
                            @Expose var responseCode: Int?=null)