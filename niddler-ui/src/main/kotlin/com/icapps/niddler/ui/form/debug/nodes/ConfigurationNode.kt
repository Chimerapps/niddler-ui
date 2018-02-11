package com.icapps.niddler.ui.form.debug.nodes

import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration

/**
 * @author nicolaverbeeck
 */
interface ConfigurationNode {

    val treeNode: TreeNode

}

class BlacklistItemNode(var regex: String,
                        isChecked: Boolean,
                        nodeBuilder: NodeBuilder) : ConfigurationNode {

    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode(regex, isChecked, this)
            .apply { setCanHaveChildren(false) }

    fun updateRegex(regex: String) {
        this.regex = regex
        treeNode.updateText(regex)
    }
}

class BlacklistRootNode(isChecked: Boolean,
                        private val nodeBuilder: NodeBuilder,
                        private val configuration: TemporaryDebuggerConfiguration) : ConfigurationNode {

    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Blacklist", isChecked, this)
            .apply { setCanHaveChildren(true) }

    private val nodes: MutableList<BlacklistItemNode> = mutableListOf()

    init {
        configuration.blacklistConfiguration.forEach {
            internalAddNode(it.item, it.enabled)
        }
    }

    fun addBlacklistItem(regex: String): BlacklistItemNode {
        configuration.addBlacklistItem(regex, true)
        return internalAddNode(regex, true)
    }

    private fun internalAddNode(regex: String, checked: Boolean): BlacklistItemNode {
        val node = BlacklistItemNode(regex, true, nodeBuilder)
        nodes.add(node)
        treeNode.addChild(node.treeNode)
        return node
    }

    fun findNode(regex: String): BlacklistItemNode? {
        return nodes.find { it.regex == regex }
    }

    fun forEachNode(function: (BlacklistItemNode) -> Unit) {
        nodes.forEach(function)
    }

}

class DelaysConfigurationRootNode(isChecked: Boolean,
                                  nodeBuilder: NodeBuilder) : ConfigurationNode {
    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Delay configuration", isChecked, this)
            .apply { setCanHaveChildren(false) }
}

class ConfigurationRootNode(nodeBuilder: NodeBuilder,
                            configuration: TemporaryDebuggerConfiguration) : ConfigurationNode {

    override val treeNode: TreeNode = nodeBuilder.createNode("Blacklist", this)
            .apply { setCanHaveChildren(true) }

    val delaysRoot: DelaysConfigurationRootNode = DelaysConfigurationRootNode(false, nodeBuilder)
    val blacklistRoot: BlacklistRootNode = BlacklistRootNode(false, nodeBuilder, configuration)

    init {
        treeNode.addChild(delaysRoot.treeNode)
        treeNode.addChild(blacklistRoot.treeNode)
    }
}
