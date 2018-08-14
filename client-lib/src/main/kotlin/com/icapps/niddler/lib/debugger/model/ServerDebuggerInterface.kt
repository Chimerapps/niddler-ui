package com.icapps.niddler.lib.debugger.model

import com.icapps.niddler.lib.utils.split

/**
 * @author nicolaverbeeck
 */
class ServerDebuggerInterface(private val service: DebuggerService) : DebuggerInterface {

    private val serverBlacklist: MutableSet<String> = mutableSetOf()
    private val knownDefaultResponses: MutableSet<String> = mutableSetOf()
    private val knownResponseIntercepts: MutableSet<String> = mutableSetOf()
    private val knownRequestIntercepts: MutableSet<String> = mutableSetOf()
    private val enabledActions: MutableSet<String> = mutableSetOf()
    private var delays: DebuggerDelays? = null

    override fun updateBlacklist(active: Iterable<String>) {
        val new = active.filterNot { serverBlacklist.contains(it) }
        val removed = serverBlacklist.filterNot { active.contains(it) }

        new.forEach(service::addBlacklistItem)
        removed.forEach(service::removeBlacklistItem)

        serverBlacklist.clear()
        serverBlacklist.addAll(active)
    }

    override fun updateDefaultResponses(items: Iterable<LocalRequestIntercept>) {
        updateActionDelta(items, knownDefaultResponses, service::removeRequestAction) {
            service.addDefaultResponse(it.regex, it.matchMethod, it.debugResponse!!, it.active)
        }
    }

    override fun updateRequestIntercepts(items: Iterable<LocalRequestIntercept>) {
        updateActionDelta(items, knownRequestIntercepts, service::removeRequestAction) {
            service.addRequestIntercept(it.regex, it.matchMethod, it.active)
        }
    }

    override fun updateResponseIntercepts(items: List<LocalResponseIntercept>) {
        updateActionDelta(items, knownResponseIntercepts, service::removeResponseAction) {
            service.addResponseIntercept(it.regex, it.matchMethod, responseCode = null, active = it.active)
        }
    }

    private fun <T : BaseAction> updateActionDelta(items: Iterable<T>,
                                                   knownItemsContainer: MutableCollection<String>,
                                                   remover: (String) -> Unit,
                                                   sender: (T) -> String) {
        val (unsentItems, knownItems) = items.split { !knownItemsContainer.contains(it.id) }
        unsentItems.forEach {
            val actionId = sender(it)
            if (it.active)
                enabledActions += actionId
            it.id = actionId
        }
        knownItems.forEach {
            if (!it.active && enabledActions.contains(it.id))
                service.muteAction(it.id)
            else if (it.active && !enabledActions.contains(it.id))
                service.unmuteAction(it.id)
        }
        val removed = knownItemsContainer.filterNot { id -> items.indexOfFirst { item -> item.id == id } != -1 }
        removed.forEach(remover)
        knownItemsContainer.clear()
        items.mapTo(knownItemsContainer) { it.id }
    }

    override fun mute() {
        service.setAllActionsMuted(true)
    }

    override fun unmute() {
        service.setAllActionsMuted(false)
    }

    override fun updateDelays(delays: DebuggerDelays?) {
        if (this.delays == delays)
            return

        this.delays = delays
        if (delays == null)
            service.updateDelays(DebuggerDelays(null, null, null))
        else
            service.updateDelays(delays)
    }

    override fun debugDelays(): DebuggerDelays? {
        return delays
    }

    override fun activate() {
        service.setActive(true)
    }

    override fun deactivate() {
        service.setActive(false)
    }

    override fun connect() {
        service.connect()
    }

    override fun disconnect() {
        service.disconnect()
    }
}