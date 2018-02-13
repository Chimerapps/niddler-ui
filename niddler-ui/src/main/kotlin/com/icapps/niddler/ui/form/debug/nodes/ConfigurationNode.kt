package com.icapps.niddler.ui.form.debug.nodes

import com.icapps.niddler.ui.debugger.model.DebuggerDelays

/**
 * @author nicolaverbeeck
 */
interface ConfigurationNode<U> {

    val treeNode: TreeNode

    var nodeData: U?

}

abstract class ConfigurationNodeWithChildren<out T : ConfigurationNode<*>, V>(
        protected val nodeBuilder: NodeBuilder) : ConfigurationNode<V> {

    private val children = mutableListOf<T>()
    override var nodeData: V? = null

    fun pushNode() {
        val node = createNode()
        children += node
        treeNode.addChild(node.treeNode)
    }

    fun popNode() {
        treeNode.removeChild(children.last().treeNode)
        children.removeAt(children.size - 1)
    }

    val childCount: Int
        get() = children.size

    fun forEachNode(function: (index: Int, item: T) -> Unit) {
        children.forEachIndexed(function)
    }

    protected abstract fun createNode(): T

    fun findNode(function: (item: T) -> Boolean): T? = children.find(function)

}

class BlacklistItemNode(var regex: String,
                        isChecked: Boolean,
                        nodeBuilder: NodeBuilder) : ConfigurationNode<String> {

    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode(regex, isChecked, this)
            .apply { setCanHaveChildren(false) }

    override var nodeData: String?
        get() = regex
        set(value) {
            regex = value!!
        }
}

class BlacklistRootNode(isChecked: Boolean,
                        nodeBuilder: NodeBuilder)
    : ConfigurationNodeWithChildren<BlacklistItemNode, Any>(nodeBuilder) {

    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Blacklist", isChecked, this)
            .apply { setCanHaveChildren(true) }

    override fun createNode(): BlacklistItemNode {
        return BlacklistItemNode("", false, nodeBuilder)
    }

}

class DelaysConfigurationRootNode(isChecked: Boolean,
                                  nodeBuilder: NodeBuilder) : ConfigurationNode<DebuggerDelays> {
    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Delay configuration", isChecked, this)
            .apply { setCanHaveChildren(false) }

    override var nodeData: DebuggerDelays? = null
}

class ConfigurationRootNode(nodeBuilder: NodeBuilder) : ConfigurationNode<Any> {

    override val treeNode: TreeNode = nodeBuilder.createNode("Blacklist", this)
            .apply { setCanHaveChildren(true) }

    override var nodeData: Any? = null

    val delaysRoot: DelaysConfigurationRootNode = DelaysConfigurationRootNode(false, nodeBuilder)
    val blacklistRoot: BlacklistRootNode = BlacklistRootNode(false, nodeBuilder)

    init {
        treeNode.addChild(delaysRoot.treeNode)
        treeNode.addChild(blacklistRoot.treeNode)
    }
}
