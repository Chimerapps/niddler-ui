package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class DefaultResponsesNode(parent: TreeNode?,
                           changeListener: (node: CheckedNode) -> Unit) : DefaultTreeNode(parent, changeListener) {

    override fun toString(): String {
        return "Default responses"
    }

    override fun getAllowsChildren(): Boolean = true

}