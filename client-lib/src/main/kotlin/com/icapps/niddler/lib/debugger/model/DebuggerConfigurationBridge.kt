package com.icapps.niddler.lib.debugger.model

import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration

/**
 * @author nicolaverbeeck
 */
class DebuggerConfigurationBridge(private val configuration: DebuggerConfiguration,
                                  private val debuggerInterface: DebuggerInterface) {

    fun apply() {
        applyDelays()
        applyBlacklist()
        applyDefaultResponses()
        applyResponseIntercepts()
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
        val defaultResponses = configuration.requestIntercept.filter { it.item.debugResponse != null }.map { it.item }
        debuggerInterface.updateDefaultResponses(defaultResponses)
    }

    private fun applyResponseIntercepts() {
        debuggerInterface.updateResponseIntercepts(configuration.responseIntercept.map { it.item })
    }

}