package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
open class CheckedNode(parent: TreeNode,
                       changeListener: (node: CheckedNode) -> Unit) : DefaultTreeNode(parent, changeListener) {

    open var isChecked: Boolean = false

}