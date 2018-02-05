package com.icapps.niddler.ui.debugger.model.saved

import com.icapps.niddler.ui.debugger.model.DebuggerDelays

/**
 * @author nicolaverbeeck
 */
interface DebuggerConfigurationProvider {

    var delayConfiguration: DisableableItem<DebuggerDelays>

    var blacklistConfiguration: List<DisableableItem<String>>

}

data class DisableableItem<out T>(val enabled: Boolean, val item: T)