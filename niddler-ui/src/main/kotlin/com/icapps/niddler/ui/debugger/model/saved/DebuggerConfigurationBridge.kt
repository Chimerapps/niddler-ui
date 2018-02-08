package com.icapps.niddler.ui.debugger.model.saved

import com.icapps.niddler.ui.debugger.model.DebuggerInterface

/**
 * @author nicolaverbeeck
 */
class DebuggerConfigurationBridge(private val configurationProvider: DebuggerConfigurationProvider,
                                  private val debuggerInterface: DebuggerInterface) {

    fun apply() {
        applyDelays()
        applyBlacklist()
    }

    private fun applyDelays() {
        val delayConfig = configurationProvider.delayConfiguration
        if (delayConfig.enabled)
            debuggerInterface.updateDelays(delayConfig.item)
        else
            debuggerInterface.updateDelays(null)
    }

    private fun applyBlacklist() {
        val blacklistItems = configurationProvider.blacklistConfiguration
        val regularExpressions = blacklistItems.filter { it.enabled }.map { it.item }
        debuggerInterface.updateBlacklist(regularExpressions)
    }

}