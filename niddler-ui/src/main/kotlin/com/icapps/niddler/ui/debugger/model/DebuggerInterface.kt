package com.icapps.niddler.ui.debugger.model

import com.google.gson.annotations.Expose

/**
 * @author nicolaverbeeck
 */
interface DebuggerInterface {

    fun updateBlacklist(active: Iterable<String>)

    fun updateDefaultResponses(items: Iterable<DefaultResponseAction>)

    fun mute()

    fun unmute()

    fun updateDelays(delays: DebuggerDelays?)

    fun debugDelays(): DebuggerDelays?
}

data class DefaultResponseAction(var id: String?,
                                 var enabled: Boolean,
                                 @Expose val regex: String?,
                                 @Expose val method: String?,
                                 @Expose val response: DebugResponse)