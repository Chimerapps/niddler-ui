package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class TimeoutConfigurationRootNode(parent: TreeNode) : CheckedNode(parent) {

    override fun toString(): String {
        return "Timeout configuration"
    }

    override fun getAllowsChildren(): Boolean {
        return false
    }
}