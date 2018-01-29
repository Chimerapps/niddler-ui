package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class BlacklistNode(private val regex: String, val enabled: Boolean, parent: TreeNode) : CheckedNode(parent) {

    override fun toString(): String {
        return regex
    }

    override fun getAllowsChildren(): Boolean {
        return false
    }
}