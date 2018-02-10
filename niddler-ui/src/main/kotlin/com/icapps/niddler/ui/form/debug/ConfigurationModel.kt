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
    val configurationRoot: ConfigurationRootNode = nodeBuilder.createRootNode()

    init {
        treeModel = DefaultTreeModel(configurationRoot as TreeNode, true)
    }

    fun isDelaysEnabled(): Boolean {
        return configurationRoot.delaysRoot.nodeCheckState
    }

    fun isBlacklistEnabled(regex: String?): Boolean {
        if (regex == null)
            return false

        return configurationRoot.blacklistRoot.isEnabled(regex)
    }

    fun setDelaysEnabled(enabled: Boolean) {
        configurationRoot.delaysRoot.nodeCheckState = enabled
        treeModel.reload()
    }
}