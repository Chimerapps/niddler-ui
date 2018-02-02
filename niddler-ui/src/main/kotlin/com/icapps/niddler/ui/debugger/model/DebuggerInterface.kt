package com.icapps.niddler.ui.debugger.model

/**
 * @author nicolaverbeeck
 */
interface DebuggerInterface {

    fun updateBlacklist(active: Iterable<String>)

    fun updateDefaultResponses(items: Iterable<DefaultResponseAction>)

    fun mute()

    fun unmute()

}

data class DefaultResponseAction(var id: String?,
                                 var enabled: Boolean,
                                 val regex: String,
                                 val response: DebugResponse)