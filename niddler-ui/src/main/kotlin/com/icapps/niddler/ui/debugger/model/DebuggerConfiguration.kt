package com.icapps.niddler.ui.debugger.model

/**
 * @author nicolaverbeeck
 */
class DebuggerConfiguration(private val service: DebuggerService) : DebuggerInterface {

    private val serverBlacklist: MutableSet<String> = mutableSetOf()

    override fun updateBlacklist(active: Iterable<String>) {
        val new = active.filterNot { serverBlacklist.contains(it) }
        val removed = serverBlacklist.filterNot { active.contains(it) }

        new.forEach(service::addBlacklistItem)
        removed.forEach(service::removeBlacklistItem)

        serverBlacklist.clear()
        serverBlacklist.addAll(active)
    }

}