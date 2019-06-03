package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationNode
import com.icapps.niddler.ui.form.debug.nodes.NodeBuilder
import com.icapps.niddler.ui.form.debug.nodes.TreeNode

/**
 * @author nicolaverbeeck
 */
class SwingNodeBuilder(private val changeListener: (node: CheckedNode) -> Unit) : NodeBuilder {

    override fun createCheckedNode(title: String,
                                   isChecked: Boolean,
                                   configurationNode: ConfigurationNode<*>): CheckedNode {
        return SwingCheckedNode(title, isChecked, configurationNode, changeListener)
    }

    override fun createNode(title: String, configurationNode: ConfigurationNode<*>): TreeNode {
        return SwingDefaultTreeNode(title, configurationNode, changeListener)
    }
}