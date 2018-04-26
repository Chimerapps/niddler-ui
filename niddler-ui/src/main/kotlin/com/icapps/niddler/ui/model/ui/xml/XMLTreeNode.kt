package com.icapps.niddler.ui.model.ui.xml

import com.icapps.niddler.ui.asEnumeration
import org.w3c.dom.Attr
import org.w3c.dom.Node
import org.w3c.dom.Text
import java.util.*
import javax.swing.tree.TreeNode

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class XMLTreeNode(private val xmlElement: Node, private val parent: TreeNode?, override var name: String?) : TreeNode, XMLNode<XMLTreeNode> {

    override var value: String? = null
    override var type: XMLNode.Type = XMLNode.Type.NODE

    private val children: MutableList<XMLTreeNode> = arrayListOf()

    init {
        name = xmlElement.asString()

        if (xmlElement.hasChildNodes()) {
            populateChildren()
        }
        if (xmlElement is Text) {
            value = xmlElement.nodeValue
            type = XMLNode.Type.TEXT
        } else {
            type = XMLNode.Type.NODE
        }
    }

    private fun populateChildren() {
        val nodeList = xmlElement.childNodes
        val numItems = nodeList.length
        for (i in 0 until numItems) {
            val item = nodeList.item(i)
            if (item is Text && item.nodeValue.isBlank())
                continue
            children.add(XMLTreeNode(item, this, item.nodeName))
        }
    }

    override fun children(): Enumeration<*> {
        return children.iterator().asEnumeration()
    }

    override fun isLeaf(): Boolean {
        return children.isEmpty()
    }

    override fun getChildCount(): Int {
        return children.size
    }

    override fun getParent(): TreeNode? {
        return parent
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        return children[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        return children.indexOf(node)
    }

    override fun getAllowsChildren(): Boolean {
        return true //No idea?
    }

    override fun toString(): String {
        return when (type) {
            XMLNode.Type.TEXT -> value ?: ""
            XMLNode.Type.NODE -> name ?: "node"
        }
    }

    fun Node.asString(): String {
        val stringBuilder = StringBuilder("");
        stringBuilder.append(nodeName)

        if (attributes != null && attributes.length > 0) {
            stringBuilder.append(" [")
            for (i: Int in 0..(attributes.length - 1)) {
                val node: Attr = attributes.item(i) as Attr
                if (i > 0) {
                    stringBuilder.append(", ")
                }
                stringBuilder.append(node.nodeName)
                stringBuilder.append("=\"")
                stringBuilder.append(node.nodeValue)
                stringBuilder.append("\"")
            }
            stringBuilder.append("]")
        }

        return stringBuilder.toString()
    }
}