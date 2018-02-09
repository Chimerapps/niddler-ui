package com.icapps.niddler.ui.debugger.model.saved

import com.google.gson.annotations.Expose
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.DefaultResponseAction

/**
 * @author nicolaverbeeck
 */
interface DebuggerConfiguration {

    var delayConfiguration: DisableableItem<DebuggerDelays>

    var blacklistConfiguration: List<DisableableItem<String>>

    var defaultResponses: List<DisableableItem<DefaultResponseAction>>

}

data class DisableableItem<T>(@Expose var enabled: Boolean, @Expose var item: T)