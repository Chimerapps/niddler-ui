package com.chimerapps.niddler.ui.model.renderer.impl.xml

import org.w3c.dom.Attr
import org.w3c.dom.Comment
import org.w3c.dom.Node
import org.w3c.dom.Text
import java.util.Enumeration
import javax.swing.tree.TreeNode

internal class XMLTreeNode(val xmlElement: Node, private val parent: TreeNode?, val name: String = xmlElement.asString()) : TreeNode {

    private val children: MutableList<XMLTreeNode> = arrayListOf()

    lateinit var value: String
        private set
    val type: Type

    init {
        if (xmlElement.hasChildNodes()) {
            populateChildren()
        }
        when (xmlElement) {
            is Text -> {
                value = xmlElement.nodeValue
                type = Type.TEXT
            }
            is Comment -> {
                value = xmlElement.nodeValue
                type = Type.COMMENT
            }
            else -> type = Type.NODE
        }
    }

    private fun populateChildren() {
        val nodeList = xmlElement.childNodes
        val numItems = nodeList.length
        for (i in 0 until numItems) {
            val item = nodeList.item(i)
            if (item is Text && item.nodeValue.isBlank())
                continue
            children.add(XMLTreeNode(nodeList.item(i), this))
        }
    }

    override fun children(): Enumeration<*> = object : Enumeration<XMLTreeNode> {

        private val it = children.iterator()

        override fun hasMoreElements(): Boolean = it.hasNext()

        override fun nextElement(): XMLTreeNode = it.next()

    }

    override fun isLeaf(): Boolean = children.isEmpty()

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = parent

    override fun getChildAt(childIndex: Int): TreeNode = children[childIndex]

    override fun getIndex(node: TreeNode?): Int = children.indexOf(node)

    override fun getAllowsChildren(): Boolean = true //No idea?

    override fun toString(): String {
        return when (type) {
            Type.TEXT -> value
            Type.NODE -> name
            Type.COMMENT -> value
        }
    }

    enum class Type {
        NODE, TEXT, COMMENT
    }
}

private fun Node.asString(): String {
    val stringBuilder = StringBuilder("");
    stringBuilder.append(nodeName)

    if (attributes != null && attributes.length > 0) {
        stringBuilder.append(" [")
        for (i: Int in 0 until attributes.length) {
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