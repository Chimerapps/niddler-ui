package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.form.debug.nodes.ConfigurationRootNode
import com.icapps.niddler.ui.form.debug.nodes.NodeBuilder
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
class ConfigurationModel(nodeBuilder: NodeBuilder) {

    val treeModel: DefaultTreeModel
    val root: ConfigurationRootNode = nodeBuilder.createRootNode()

    init {
        treeModel = DefaultTreeModel(root as TreeNode, true)
    }

    fun isDelaysEnabled(): Boolean {
        return root.delaysRoot.nodeCheckState
    }

    fun isBlacklistEnabled(regex: String?): Boolean {
        if (regex == null)
            return false

        return root.blacklistRoot.isEnabled(regex)
    }

    fun setDelaysEnabled(enabled: Boolean) {
        root.delaysRoot.nodeCheckState = enabled
        treeModel.reload()
    }
}