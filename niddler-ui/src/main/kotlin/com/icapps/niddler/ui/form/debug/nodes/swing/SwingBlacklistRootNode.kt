package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration
import com.icapps.niddler.ui.form.debug.nodes.BlacklistItemNode
import com.icapps.niddler.ui.form.debug.nodes.BlacklistRootNode
import com.icapps.niddler.ui.form.debug.nodes.CheckedNode

/**
 * @author nicolaverbeeck
 */
class SwingBlacklistRootNode(isChecked: Boolean,
                             private val configuration: TemporaryDebuggerConfiguration,
                             private val changeListener: (node: CheckedNode) -> Unit)
    : BlacklistRootNode, SwingCheckedNode("Blacklist", isChecked, changeListener) {

    private val nodes: MutableList<SwingBlacklistNode> = mutableListOf()

    init {
        configuration.blacklistConfiguration.forEach {
            internalAddNode(it.item, it.enabled)
        }
    }

    override fun addBlacklistItem(regex: String): BlacklistItemNode {
        configuration.addBlacklistItem(regex, true)
        return internalAddNode(regex, true)
    }

    private fun internalAddNode(regex: String, checked: Boolean): BlacklistItemNode {
        val node = SwingBlacklistNode(regex, true, changeListener)
        nodes.add(node)
        addChild(node)
        return node
    }

    override fun isEnabled(regex: String): Boolean {
        return nodes.find { it.regex == regex }?.nodeCheckState == true
    }

    override fun getAllowsChildren(): Boolean = true

}