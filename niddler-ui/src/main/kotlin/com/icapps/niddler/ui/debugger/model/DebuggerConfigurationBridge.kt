package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration

/**
 * @author nicolaverbeeck
 */
class DebuggerConfigurationBridge(private val configuration: DebuggerConfiguration,
                                  private val debuggerInterface: DebuggerInterface) {

    fun apply() {
        applyDelays()
        applyBlacklist()
        applyDefaultResponses()
    }

    private fun applyDelays() {
        val delayConfig = configuration.delayConfiguration
        if (delayConfig.enabled)
            debuggerInterface.updateDelays(delayConfig.item)
        else
            debuggerInterface.updateDelays(null)
    }

    private fun applyBlacklist() {
        val blacklistItems = configuration.blacklistConfiguration
        val regularExpressions = blacklistItems.filter { it.enabled }.map { it.item }
        debuggerInterface.updateBlacklist(regularExpressions)
    }

    private fun applyDefaultResponses() {
        val defaultResponses = configuration.defaultResponses
        val activeResponses = defaultResponses.filter { it.enabled }.map { it.item }
        debuggerInterface.updateDefaultResponses(activeResponses)
    }

}