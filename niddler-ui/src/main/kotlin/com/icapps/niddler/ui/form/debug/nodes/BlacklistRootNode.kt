package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class BlacklistRootNode(parent: TreeNode) : DefaultTreeNode(parent) {

    init {
        addChild(BlacklistNode("*.json", false, this))
    }

    override fun toString(): String {
        return "Blacklist"
    }

    override fun getAllowsChildren(): Boolean = true

}