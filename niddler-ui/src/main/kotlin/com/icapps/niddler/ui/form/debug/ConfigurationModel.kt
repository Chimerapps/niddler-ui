package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationNode
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationRootNode
import com.icapps.niddler.ui.form.debug.nodes.NodeBuilder
import com.icapps.niddler.ui.path
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
open class ConfigurationModel(configuration: TemporaryDebuggerConfiguration,
                              nodeBuilder: NodeBuilder) {

    lateinit var tree: JTree
    val treeModel: DefaultTreeModel
    val configurationRoot: ConfigurationRootNode = ConfigurationRootNode(nodeBuilder, configuration)

    init {
        treeModel = DefaultTreeModel(configurationRoot.treeNode as TreeNode, true)
    }

    fun isDelaysEnabled(): Boolean {
        return configurationRoot.delaysRoot.treeNode.nodeCheckState
    }

    fun isBlacklistEnabled(regex: String?): Boolean {
        if (regex == null)
            return false

        return configurationRoot.blacklistRoot.findNode(regex)?.treeNode?.nodeCheckState == true
    }

    fun setDelaysEnabled(enabled: Boolean) {
        configurationRoot.delaysRoot.treeNode.nodeCheckState = enabled
        nodeChanged(configurationRoot.delaysRoot.treeNode)
    }

    fun setBlacklistEnabled(regex: String, enabled: Boolean) {
        val node = configurationRoot.blacklistRoot.findNode(regex) ?: return
        node.treeNode.nodeCheckState = enabled
        nodeChanged(node.treeNode)
    }

    open fun nodeChanged(node: com.icapps.niddler.ui.form.debug.nodes.TreeNode) {
        val path = (node as TreeNode).path()
        tree.startEditingAtPath(path)
        tree.stopEditing()
        tree.invalidate()
        tree.validate()
        tree.repaint()
    }

    fun forEachLeafNode(function: (configurationNode: ConfigurationNode) -> Unit) {
        function(configurationRoot.delaysRoot)
        configurationRoot.blacklistRoot.forEachNode(function)
    }
}