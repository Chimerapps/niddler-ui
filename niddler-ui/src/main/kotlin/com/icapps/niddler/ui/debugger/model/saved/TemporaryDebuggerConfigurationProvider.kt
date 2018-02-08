package com.icapps.niddler.ui.debugger.model.saved

import com.icapps.niddler.ui.debugger.model.DebugResponse
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.DefaultResponseAction

/**
 * @author nicolaverbeeck
 */
class TemporaryDebuggerConfigurationProvider(delegate: DebuggerConfigurationProvider) : DebuggerConfigurationProvider {

    private val internalBlacklist: MutableList<DisableableItem<String>> = mutableListOf()
    private val internalDefaultResponses: MutableList<DisableableItem<DefaultResponseAction>> = mutableListOf()

    init {
        delegate.blacklistConfiguration.mapTo(internalBlacklist) { it.copy() }
        delegate.defaultResponses.mapTo(internalDefaultResponses) { it.copy() }
    }

    override var delayConfiguration: DisableableItem<DebuggerDelays> = delegate.delayConfiguration.copy()

    override var blacklistConfiguration: List<DisableableItem<String>>
        get() = internalBlacklist
        set(value) = throw IllegalStateException("Do not set blacklist directly")

    override var defaultResponses: List<DisableableItem<DefaultResponseAction>>
        get() = internalDefaultResponses
        set(value) = throw IllegalStateException("Do not set default responses directly")


    fun addBlacklistItem(regex: String, enabled: Boolean): DisableableItem<String>? {
        if (internalBlacklist.find { it.item == regex } != null)
            return null

        val item = DisableableItem(enabled, regex)
        internalBlacklist += item
        return item
    }

    fun removeBlacklistItem(regex: DisableableItem<String>) {
        internalBlacklist.removeIf { it === regex }
    }

    fun addDefaultResponse(regex: String?,
                           method: String?,
                           defaultResponse: DebugResponse,
                           enabled: Boolean): DisableableItem<DefaultResponseAction>? {
        if (internalDefaultResponses.find { it.item.regex == regex && it.item.method == method } != null)
            return null

        val item = DisableableItem(enabled, DefaultResponseAction(id = null,
                enabled = enabled,
                regex = regex,
                method = method,
                response = defaultResponse))
        internalDefaultResponses += item
        return item
    }

    fun removeDefaultResponse(item: DisableableItem<DefaultResponseAction>) {
        internalDefaultResponses.removeIf { it === item }
    }
}
