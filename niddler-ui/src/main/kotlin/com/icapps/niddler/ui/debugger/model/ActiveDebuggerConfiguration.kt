package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.split

/**
 * @author nicolaverbeeck
 */
class ActiveDebuggerConfiguration(private val service: DebuggerService) : DebuggerInterface {

    private val serverBlacklist: MutableSet<String> = mutableSetOf()
    private val knownDefaultResponses: MutableSet<String> = mutableSetOf()
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

    override fun updateDefaultResponses(items: Iterable<DefaultResponseAction>) {
        val (unsentItems, knownItems) = items.split { !knownDefaultResponses.contains(it.id) }
        unsentItems.forEach {
            val actionId = service.addDefaultResponse(it.regex, it.method, it.response, it.enabled)
            if (it.enabled)
                enabledActions += actionId
            it.id = actionId
        }
        knownItems.forEach {
            if (!it.enabled && enabledActions.contains(it.id))
                service.muteAction(it.id!!)
            else if (it.enabled && !enabledActions.contains(it.id))
                service.unmuteAction(it.id!!)
        }

        val removed = knownDefaultResponses.filterNot { id -> items.indexOfFirst { it.id == id } != -1 }
        removed.forEach(service::removeRequestAction)
        knownDefaultResponses.clear()
        items.mapTo(knownDefaultResponses) { it.id!! }
    }

    override fun mute() {
        service.setAllActionsMuted(true)
    }

    override fun unmute() {
        service.setAllActionsMuted(false)
    }

    override fun updateDelays(delays: DebuggerDelays?) {
        this.delays = delays
        if (delays == null)
            service.updateDelays(DebuggerDelays(null, null, null))
        else
            service.updateDelays(delays)
    }

    override fun debugDelays(): DebuggerDelays? {
        return delays
    }
}