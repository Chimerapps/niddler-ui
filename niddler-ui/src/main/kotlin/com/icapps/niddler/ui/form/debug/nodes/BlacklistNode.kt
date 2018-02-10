package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class BlacklistNode(val regex: String, val enabled: Boolean, parent: TreeNode,
                    changeListener: (node: CheckedNode) -> Unit) : CheckedNode(parent, changeListener) {

    override fun toString(): String {
        return regex
    }

    override fun getAllowsChildren(): Boolean {
        return false
    }
}