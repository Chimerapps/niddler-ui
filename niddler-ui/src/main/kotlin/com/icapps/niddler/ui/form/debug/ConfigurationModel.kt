package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.debugger.model.DebuggerConfigurationModelAdapter
import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration
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
    val configurationRoot: ConfigurationRootNode = ConfigurationRootNode(nodeBuilder)
    private val adapter = DebuggerConfigurationModelAdapter(configuration, this)

    init {
        treeModel = DefaultTreeModel(configurationRoot.treeNode as TreeNode, true)
    }

    fun onConfigurationChanged() {
        adapter.syncNodes()
    }

    open fun nodeChanged(node: com.icapps.niddler.ui.form.debug.nodes.TreeNode) {
        val path = (node as TreeNode).path()
        treeModel.nodeChanged(node as TreeNode)
        tree.startEditingAtPath(path)
        tree.stopEditing()
    }

    fun structureChanged(treeNode: com.icapps.niddler.ui.form.debug.nodes.TreeNode) {
        treeModel.nodeStructureChanged(treeNode as TreeNode)
    }

}