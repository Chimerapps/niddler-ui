package com.icapps.niddler.lib.debugger.model.saved

import com.google.gson.annotations.Expose
import com.icapps.niddler.lib.debugger.model.DebuggerDelays
import com.icapps.niddler.lib.debugger.model.LocalRequestIntercept
import com.icapps.niddler.lib.debugger.model.LocalRequestOverride
import com.icapps.niddler.lib.debugger.model.LocalResponseIntercept

/**
 * @author nicolaverbeeck
 */
interface DebuggerConfiguration {

    var delayConfiguration: DisableableItem<DebuggerDelays>

    var blacklistConfiguration: List<DisableableItem<String>>

    var requestOverride: List<DisableableItem<LocalRequestOverride>>

    var requestIntercept: List<DisableableItem<LocalRequestIntercept>>

    var responseIntercept: List<DisableableItem<LocalResponseIntercept>>

}

data class DisableableItem<T>(@Expose var enabled: Boolean, @Expose var item: T)