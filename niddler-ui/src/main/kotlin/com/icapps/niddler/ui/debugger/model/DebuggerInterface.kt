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

open class BaseActionPayload(@Expose var id: String,
                             @Expose var active: Boolean,
                             @Expose var regex: String?,
                             @Expose var matchMethod: String?,
                             @Expose var repeatCount: Int?)

data class DefaultResponseAction(var id: String?,
                                 var enabled: Boolean,
                                 @Expose val regex: String?,
                                 @Expose val method: String?,
                                 @Expose val response: DebugResponse)

class RequestOverride(id: String = "",
                      active: Boolean = false,
                      regex: String? = null,
                      matchMethod: String? = null,
                      repeatCount: Int? = null,
                      var debugRequest: DebugRequest? = null)
    : BaseActionPayload(id, active, regex, matchMethod, repeatCount) {
    override fun toString(): String {
        return regex ?: matchMethod ?: ""
    }
}