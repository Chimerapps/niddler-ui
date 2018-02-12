package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration

/**
 * @author nicolaverbeeck
 */
interface ModifiableDebuggerConfiguration : DebuggerConfiguration {

    fun addBlacklistItem(regex: String, enabled: Boolean): Boolean

    fun removeBlacklistItem(regex: String)

    fun setBlacklistActive(regex: String, active: Boolean)

    fun changeRegex(fromRegex: String, toRegex: String)


    fun setDebuggerDelaysActive(active: Boolean)

    fun updateDebuggerDelays(delays: DebuggerDelays)

}
