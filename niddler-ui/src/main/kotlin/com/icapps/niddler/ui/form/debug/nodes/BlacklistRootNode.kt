package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class BlacklistRootNode(parent: TreeNode,
                        private val changeListener: (node: CheckedNode) -> Unit)
    : CheckedNode(parent, changeListener) {

    private val nodes: MutableList<BlacklistNode> = mutableListOf()

    fun addBlacklistItem(regex: String) {
        val node = BlacklistNode(regex, true, this, changeListener)
        nodes.add(node)
        addChild(node)
    }

    fun isEnabled(regex: String): Boolean {
        return nodes.find { it.regex == regex }?.enabled == true
    }

    override fun toString(): String {
        return "Blacklist"
    }

    override fun getAllowsChildren(): Boolean = true

}