package com.icapps.niddler.ui.form.debug.nodes

/**
 * @author nicolaverbeeck
 */
interface TreeNode {

    fun addChild(node: TreeNode)

    fun updateText(text: String)

    val configurationNode: ConfigurationNode

    fun setCanHaveChildren(canHaveChildren: Boolean)
}

interface CheckedNode : TreeNode {
    var nodeCheckState: Boolean
}

interface NodeBuilder {

    fun createCheckedNode(title: String, isChecked: Boolean, configurationNode: ConfigurationNode): CheckedNode

    fun createNode(title: String, configurationNode: ConfigurationNode): TreeNode

}