package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import java.util.ArrayList
import java.util.Collections
import java.util.Enumeration
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * @author Nicola Verbeeck
 * @date 17/11/16.
 */
class LinkedMessagesModel : DefaultTreeModel(DefaultMutableTreeNode()), MessagesModel {

    private var messages: Map<String, List<ParsedNiddlerMessage>> = Collections.emptyMap()
    private lateinit var container: NiddlerMessageStorage<ParsedNiddlerMessage>

    override fun updateMessages(messages: NiddlerMessageStorage<ParsedNiddlerMessage>, urlHider: BaseUrlHider) {
        this.messages = messages.messagesLinked
        container = messages

        //TODO fix url hider
        setRoot(MessageRootNode(this.messages))
    }

}

class MessageRootNode(children: Map<String, List<ParsedNiddlerMessage>>) : TreeNode {

    private val childNodes: List<RequestNode> = children.map { RequestNode(this, it.key, it.value) }

    override fun children(): Enumeration<*> {
        val iterator = childNodes.iterator()
        return object : Enumeration<TreeNode> {
            override fun nextElement(): TreeNode {
                return iterator.next()
            }

            override fun hasMoreElements(): Boolean {
                return iterator.hasNext()
            }
        }
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getChildCount(): Int {
        return childNodes.size
    }

    override fun getParent(): TreeNode? {
        return null
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        return childNodes[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        return childNodes.indexOf(node)
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }

}

class NetworkRequestNode(parent: TreeNode, requestId: String, messages: List<ParsedNiddlerMessage>) : RequestNode(parent, requestId, messages)
class NetworkResponseNode(parent: TreeNode, message: ParsedNiddlerMessage) : ResponseNode(parent, message)

open class RequestNode(private val parent: TreeNode, requestId: String, messages: List<ParsedNiddlerMessage>) : TreeNode {

    var request: ParsedNiddlerMessage? = messages.find { it.isRequest }

    private val responseNodes: List<ResponseNode> = messages.filter { !it.isRequest }.map {
        ResponseNode(this, it)
    }
    private val networkRequestNode: RequestNode?
    private val networkReplyNode: ResponseNode?

    private val nodes: List<TreeNode>

    init {
        val networkRequest = responseNodes.getOrNull(0)?.message?.parsedNetworkRequest
        val networkReply = responseNodes.getOrNull(0)?.message?.parsedNetworkReply
        networkRequestNode = if (networkRequest == null) null else NetworkRequestNode(this, requestId, listOf(networkRequest))
        networkReplyNode = if (networkReply == null) null else NetworkResponseNode(this, networkReply)

        val actualNodes = ArrayList<TreeNode>(responseNodes.size)
        if (networkRequestNode != null)
            actualNodes.add(networkRequestNode)
        if (networkReplyNode != null)
            actualNodes.add(networkReplyNode)
        actualNodes += responseNodes

        nodes = actualNodes
    }

    override fun children(): Enumeration<*> {
        val iterator = nodes.iterator()
        return object : Enumeration<TreeNode> {
            override fun nextElement(): TreeNode {
                return iterator.next()
            }

            override fun hasMoreElements(): Boolean {
                return iterator.hasNext()
            }
        }
    }

    override fun isLeaf(): Boolean {
        return nodes.isEmpty()
    }

    override fun getChildCount(): Int {
        return nodes.size
    }

    override fun getParent(): TreeNode {
        return parent
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        return nodes[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        return nodes.indexOf(node)
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }
}

open class ResponseNode(private val parent: TreeNode, val message: ParsedNiddlerMessage) : TreeNode {

    override fun children(): Enumeration<*> {
        return object : Enumeration<TreeNode> {
            override fun nextElement(): TreeNode? {
                return null
            }

            override fun hasMoreElements(): Boolean {
                return false
            }
        }
    }

    override fun isLeaf(): Boolean {
        return true
    }

    override fun getChildCount(): Int {
        return 0
    }

    override fun getParent(): TreeNode {
        return parent
    }

    override fun getChildAt(childIndex: Int): TreeNode? {
        return null
    }

    override fun getIndex(node: TreeNode?): Int {
        return -1
    }

    override fun getAllowsChildren(): Boolean {
        return false
    }
}
