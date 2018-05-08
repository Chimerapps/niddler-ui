package com.icapps.niddler.lib.debugger.model

import com.icapps.niddler.lib.utils.split

/**
 * @author nicolaverbeeck
 */
class ServerDebuggerInterface(private val service: DebuggerService) : DebuggerInterface {

    private val serverBlacklist: MutableSet<String> = mutableSetOf()
    private val knownDefaultResponses: MutableSet<String> = mutableSetOf()
    private val knownResponseIntercepts: MutableSet<String> = mutableSetOf()
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
        val (unsentItems, knownItems) = items.split { !knownDefaultResponses.contains(it.id) }
        unsentItems.forEach {
            val actionId = service.addDefaultResponse(it.regex, it.matchMethod, it.debugResponse!!, it.active)
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

        val removed = knownDefaultResponses.filterNot { id -> items.indexOfFirst { it.id == id } != -1 }
        removed.forEach(service::removeRequestAction)
        knownDefaultResponses.clear()
        items.mapTo(knownDefaultResponses) { it.id }
    }

    override fun updateResponseIntercepts(items: List<LocalResponseIntercept>) {
        val (unsetItems, knownItems) = items.split { !knownResponseIntercepts.contains(it.id) }
        unsetItems.forEach {
            val actionId = service.addResponseIntercept(it.regex, it.matchMethod, responseCode = null, active = it.active)
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

        val removed = knownResponseIntercepts.filterNot { id -> items.indexOfFirst { it.id == id } != -1 }
        removed.forEach(service::removeResponseAction)
        knownResponseIntercepts.clear()
        items.mapTo(knownResponseIntercepts) { it.id }
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