package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class DelaysConfigurationRootNode(parent: TreeNode,
                                  changeListener: (node: CheckedNode) -> Unit) : CheckedNode(parent, changeListener) {

    override fun toString(): String {
        return "Delay configuration"
    }

    override fun getAllowsChildren(): Boolean {
        return false
    }
}