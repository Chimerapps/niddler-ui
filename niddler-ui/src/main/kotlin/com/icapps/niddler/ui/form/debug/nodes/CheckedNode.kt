package com.icapps.niddler.ui.form.debug.nodes

import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
open class CheckedNode(parent: TreeNode) : DefaultTreeNode(parent) {

    open var isChecked: Boolean = false

}