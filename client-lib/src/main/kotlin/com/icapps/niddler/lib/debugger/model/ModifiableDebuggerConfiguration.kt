package com.icapps.niddler.lib.debugger.model

import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration

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


    fun addRequestOverride(urlRegex: String?, method: String?, enabled: Boolean): String

    fun removeRequestOverride(id: String)

    fun setRequestOverrideActive(id: String, active: Boolean)

    fun modifyRequestOverrideAction(override: LocalRequestOverride, enabled: Boolean)


    fun addRequestIntercept(urlRegex: String?, method: String?, enabled: Boolean): String

    fun removeRequestIntercept(id: String)

    fun setRequestInterceptActive(id: String, active: Boolean)

    fun modifyRequestIntercept(intercept: LocalRequestIntercept, enabled: Boolean)


    fun addResponseIntercept(urlRegex: String?, method: String?, enabled: Boolean): String

    fun removeResponseIntercept(id: String)

    fun setResponseInterceptActive(id: String, active: Boolean)

    fun modifyResponseIntercept(intercept: LocalResponseIntercept, enabled: Boolean)
}
