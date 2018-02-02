package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.split

/**
 * @author nicolaverbeeck
 */
class DebuggerConfiguration(private val service: DebuggerService) : DebuggerInterface {

    private val serverBlacklist: MutableSet<String> = mutableSetOf()
    private val knownDefaultResponses: MutableSet<String> = mutableSetOf()
    private val enabledActions: MutableSet<String> = mutableSetOf()

    override fun updateBlacklist(active: Iterable<String>) {
        val new = active.filterNot { serverBlacklist.contains(it) }
        val removed = serverBlacklist.filterNot { active.contains(it) }

        new.forEach(service::addBlacklistItem)
        removed.forEach(service::removeBlacklistItem)

        serverBlacklist.clear()
        serverBlacklist.addAll(active)
    }

    override fun updateDefaultResponses(items: Iterable<DefaultResponseAction>) {
        val (unsentItems, knownItems) = items.split { it.id == null }
        unsentItems.forEach {
            val actionId = service.addDefaultResponse(it.regex, it.response, it.enabled)
            if (it.enabled)
                enabledActions += actionId
        }
        knownItems.forEach {
            if (!it.enabled && enabledActions.contains(it.id))
                service.muteAction(it.id!!)
        }
        val removed = knownDefaultResponses.filterNot { id -> items.indexOfFirst { it.id == id } != 0 }
        removed.forEach(service::removeRequestOverrideMethod)
        knownDefaultResponses.clear()
        items.mapTo(knownDefaultResponses) { it.id!! }
    }

    override fun mute() {
        service.setAllActionsMuted(true)
    }

    override fun unmute() {
        service.setAllActionsMuted(false)
    }
}