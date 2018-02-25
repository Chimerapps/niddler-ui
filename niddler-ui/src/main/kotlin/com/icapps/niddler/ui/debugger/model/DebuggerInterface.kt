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

interface BaseMatcher {
    var regex: String?
    var matchMethod: String?
}

interface ResponseMatcher : BaseMatcher {
    var responseCode: Int?
}

data class DefaultResponseAction(var id: String?,
                                 var enabled: Boolean,
                                 @Expose val regex: String?,
                                 @Expose val method: String?,
                                 val response: DebugResponse)

data class LocalRequestOverride(@Expose var id: String = "",
                                @Expose override var regex: String? = null,
                                @Expose override var matchMethod: String? = null,
                                @Expose var repeatCount: Int? = null,
                                @Expose var debugRequest: DebugRequest? = null) : BaseMatcher {
    override fun toString(): String {
        return regex ?: matchMethod ?: ""
    }
}

data class LocalResponseOverride(@Expose var id: String = "",
                                 @Expose var active: Boolean = false,
                                 @Expose override var regex: String? = null,
                                 @Expose override var matchMethod: String? = null,
                                 @Expose var repeatCount: Int? = null,
                                 @Expose var debugResponse: DebugResponse? = null) : BaseMatcher

data class LocalRequestIntercept(@Expose var id: String = "",
                                 @Expose var active: Boolean = false,
                                 @Expose override var regex: String? = null,
                                 @Expose override var matchMethod: String? = null,
                                 @Expose override var responseCode: Int? = null) : ResponseMatcher