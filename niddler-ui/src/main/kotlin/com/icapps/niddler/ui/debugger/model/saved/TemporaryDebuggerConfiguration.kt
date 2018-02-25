package com.icapps.niddler.ui.debugger.model.saved

import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.DefaultResponseAction
import com.icapps.niddler.ui.debugger.model.LocalRequestOverride
import com.icapps.niddler.ui.debugger.model.ModifiableDebuggerConfiguration
import java.util.*

/**
 * @author nicolaverbeeck
 */
class TemporaryDebuggerConfiguration(delegate: DebuggerConfiguration,
                                     private val changeListener: () -> Unit)
    : ModifiableDebuggerConfiguration, DebuggerConfiguration {

    private val internalBlacklist: MutableList<DisableableItem<String>> = mutableListOf()
    private val internalDefaultResponses: MutableList<DisableableItem<DefaultResponseAction>> = mutableListOf()
    private val internalRequestOverride: MutableList<DisableableItem<LocalRequestOverride>> = mutableListOf()
    private var internalDelayConfiguration: DisableableItem<DebuggerDelays>

    override var delayConfiguration: DisableableItem<DebuggerDelays>
        get() = internalDelayConfiguration
        set(value) {
            throw IllegalStateException("Setting not allowed")
        }
    override var blacklistConfiguration: List<DisableableItem<String>>
        get() = internalBlacklist
        set(value) {
            throw IllegalStateException("Setting not allowed")
        }
    override var defaultResponses: List<DisableableItem<DefaultResponseAction>>
        get() = internalDefaultResponses
        set(value) {
            throw IllegalStateException("Setting not allowed")
        }
    override var requestOverride: List<DisableableItem<LocalRequestOverride>>
        get() = internalRequestOverride
        set(value) {
            throw IllegalStateException("Setting not allowed")
        }

    init {
        delegate.blacklistConfiguration.mapTo(internalBlacklist) { it.copy() }
        delegate.defaultResponses.mapTo(internalDefaultResponses) { it.copy() }
        delegate.requestOverride.mapTo(internalRequestOverride) { it.copy() }
        internalDelayConfiguration = delegate.delayConfiguration
    }

    override fun addBlacklistItem(regex: String, enabled: Boolean): Boolean {
        if (internalBlacklist.find { it.item == regex } != null)
            return false

        val item = DisableableItem(enabled, regex)
        internalBlacklist += item
        notifyChange()
        return true
    }

    override fun removeBlacklistItem(regex: String) {
        if (internalBlacklist.removeIf { it.item == regex })
            notifyChange()
    }

    override fun setBlacklistActive(regex: String, active: Boolean) {
        val item = internalBlacklist.find { it.item == regex }
        item?.let {
            it.enabled = active
            notifyChange()
        }
    }

    override fun changeRegex(fromRegex: String, toRegex: String) {
        if (fromRegex == toRegex)
            return

        val index = internalBlacklist.indexOfFirst { it.item == fromRegex }
        if (index < 0)
            return

        internalBlacklist[index] = internalBlacklist[index].copy(item = toRegex)
        notifyChange()
    }

    override fun setDebuggerDelaysActive(active: Boolean) {
        if (internalDelayConfiguration.enabled == active)
            return
        internalDelayConfiguration.enabled = active
        notifyChange()
    }

    override fun updateDebuggerDelays(delays: DebuggerDelays) {
        if (internalDelayConfiguration.item == delays)
            return

        internalDelayConfiguration = internalDelayConfiguration.copy(item = delays.copy())
        notifyChange()
    }

    override fun addRequestOverride(urlRegex: String?, method: String?, enabled: Boolean): String {
        val id = UUID.randomUUID().toString()

        internalRequestOverride += DisableableItem(enabled, LocalRequestOverride(id,
                urlRegex, method, -1, debugRequest = null))

        notifyChange()
        return id
    }

    override fun removeRequestOverride(id: String) {
        if (internalRequestOverride.removeIf { it.item.id == id })
            notifyChange()
    }

    override fun setRequestOverrideActive(id: String, active: Boolean) {
        val item = internalRequestOverride.find { it.item.id == id }
        item?.let {
            it.enabled = active
            notifyChange()
        }
    }

    override fun modifyRequestOverrideAction(override: LocalRequestOverride, enabled: Boolean) {
        val index = internalRequestOverride.indexOfFirst { it.item.id == override.id }
        internalRequestOverride[index] = DisableableItem(enabled, override.copy())
    }

    private fun notifyChange() {
        changeListener()
    }
}
