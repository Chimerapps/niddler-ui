package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.form.debug.nodes.ConfigurationRootNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class ConfigurationModel {

    val treeModel: TreeModel
    val root: TreeNode

    init {
        root = ConfigurationRootNode()
        treeModel = DefaultTreeModel(root, true)
    }
}