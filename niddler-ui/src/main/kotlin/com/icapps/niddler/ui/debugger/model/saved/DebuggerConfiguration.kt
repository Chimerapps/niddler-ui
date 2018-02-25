package com.icapps.niddler.ui.debugger.model.saved

import com.google.gson.annotations.Expose
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.LocalRequestOverride
import com.icapps.niddler.ui.debugger.model.LocalRequestIntercept

/**
 * @author nicolaverbeeck
 */
interface DebuggerConfiguration {

    var delayConfiguration: DisableableItem<DebuggerDelays>

    var blacklistConfiguration: List<DisableableItem<String>>

    var requestOverride: List<DisableableItem<LocalRequestOverride>>

    var requestIntercept: List<DisableableItem<LocalRequestIntercept>>

}

data class DisableableItem<T>(@Expose var enabled: Boolean, @Expose var item: T)