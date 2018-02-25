package com.icapps.niddler.ui.form.debug.nodes

import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.RequestIntercept
import com.icapps.niddler.ui.debugger.model.RequestOverride
import com.icapps.niddler.ui.debugger.model.ResponseOverride

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

class RequestOverrideNode(var requestOverride: RequestOverride,
                          isChecked: Boolean,
                          nodeBuilder: NodeBuilder) : ConfigurationNode<RequestOverride> {
    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode(requestOverride.regex
            ?: requestOverride.matchMethod ?: "", isChecked, this)
            .apply { setCanHaveChildren(false) }

    override var nodeData: RequestOverride?
        get() = requestOverride
        set(value) {
            requestOverride = value!!
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

class RequestOverrideRootNode(isChecked: Boolean,
                              nodeBuilder: NodeBuilder)
    : ConfigurationNodeWithChildren<RequestOverrideNode, Any>(nodeBuilder) {

    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Request overrides", isChecked, this)
            .apply { setCanHaveChildren(true) }

    override fun createNode(): RequestOverrideNode {
        return RequestOverrideNode(RequestOverride(), false, nodeBuilder)
    }
}

class ResponseOverrideNode(var responseOverride: ResponseOverride,
                           isChecked: Boolean,
                           nodeBuilder: NodeBuilder) : ConfigurationNode<ResponseOverride> {
    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode(responseOverride.regex
            ?: responseOverride.matchMethod ?: "", isChecked, this)
            .apply { setCanHaveChildren(false) }

    override var nodeData: ResponseOverride?
        get() = responseOverride
        set(value) {
            responseOverride = value!!
        }
}

class ResponseOverrideRootNode(isChecked: Boolean,
                               nodeBuilder: NodeBuilder)
    : ConfigurationNodeWithChildren<ResponseOverrideNode, Any>(nodeBuilder) {

    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Response overrides", isChecked, this)
            .apply { setCanHaveChildren(true) }

    override fun createNode(): ResponseOverrideNode {
        return ResponseOverrideNode(ResponseOverride(), false, nodeBuilder)
    }
}

class ResponseInterceptNode(var requestIntercept: RequestIntercept,
                            isChecked: Boolean,
                            nodeBuilder: NodeBuilder) : ConfigurationNode<RequestIntercept> {
    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode(requestIntercept.regex
            ?: requestIntercept.matchMethod ?: requestIntercept.responseCode?.toString() ?: "", isChecked, this)
            .apply { setCanHaveChildren(false) }

    override var nodeData: RequestIntercept?
        get() = requestIntercept
        set(value) {
            requestIntercept = value!!
        }
}

class ResponseInterceptRootNode(isChecked: Boolean,
                                nodeBuilder: NodeBuilder)
    : ConfigurationNodeWithChildren<ResponseInterceptNode, Any>(nodeBuilder) {

    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Response intercept", isChecked, this)
            .apply { setCanHaveChildren(true) }

    override fun createNode(): ResponseInterceptNode {
        return ResponseInterceptNode(RequestIntercept(), false, nodeBuilder)
    }
}

class DelaysConfigurationRootNode(isChecked: Boolean,
                                  nodeBuilder: NodeBuilder) : ConfigurationNode<DebuggerDelays> {
    override val treeNode: CheckedNode = nodeBuilder.createCheckedNode("Delay configuration", isChecked, this)
            .apply { setCanHaveChildren(false) }

    override var nodeData: DebuggerDelays? = null
}

class ConfigurationRootNode(nodeBuilder: NodeBuilder) : ConfigurationNode<Any> {

    override val treeNode: TreeNode = nodeBuilder.createNode("", this)
            .apply { setCanHaveChildren(true) }

    override var nodeData: Any? = null

    val delaysRoot = DelaysConfigurationRootNode(false, nodeBuilder)
    val blacklistRoot = BlacklistRootNode(false, nodeBuilder)
    val requestOverrideRoot = RequestOverrideRootNode(false, nodeBuilder)
    val responseOverrideRoot = ResponseOverrideRootNode(false, nodeBuilder)
    val responseInterceptRoot = ResponseInterceptRootNode(false, nodeBuilder)

    init {
        treeNode.addChild(delaysRoot.treeNode)
        treeNode.addChild(blacklistRoot.treeNode)
        treeNode.addChild(requestOverrideRoot.treeNode)
        treeNode.addChild(responseOverrideRoot.treeNode)
        treeNode.addChild(responseInterceptRoot.treeNode)
    }
}
