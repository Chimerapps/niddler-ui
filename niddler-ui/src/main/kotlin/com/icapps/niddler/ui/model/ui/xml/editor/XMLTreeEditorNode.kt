package com.icapps.niddler.ui.model.ui.xml.editor

import com.icapps.niddler.ui.asEnumeration
import com.icapps.niddler.ui.model.ui.xml.XMLNode
import org.apache.http.util.TextUtils
import org.w3c.dom.Attr
import org.w3c.dom.Node
import org.w3c.dom.Text
import java.util.*
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

/**
 * @author Koen Van Looveren
 */
class XMLTreeEditorNode(private val xmlElement: Node, private var parent: TreeNode?, override var name: String?) : MutableTreeNode, XMLNode<XMLTreeEditorNode> {

    override var value: String? = null
    override var type: XMLNode.Type = XMLNode.Type.NODE

    private val children: MutableList<XMLTreeEditorNode> = arrayListOf()

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
            children.add(XMLTreeEditorNode(item, this, item.nodeName))
        }
    }

    //region MutableTreeNode
    override fun insert(child: MutableTreeNode?, index: Int) {
        if (child == null) {
            return
        }
        val oldParent = child.parent as MutableTreeNode

        oldParent.remove(child)
        child.setParent(this)
        children.add(index, child as XMLTreeEditorNode)
    }

    override fun setParent(newParent: MutableTreeNode?) {
        parent = newParent
    }

    override fun setUserObject(newObject: Any?) {
        if (newObject is XMLTreeEditor.EditedXML) {
            type = newObject.type
            if (!TextUtils.isEmpty(newObject.name)) {
                when (type) {
                    XMLNode.Type.NODE -> name = newObject.name
                    XMLNode.Type.TEXT -> value = newObject.name
                }
            }
        }
    }

    override fun remove(index: Int) {
        val child = getChildAt(index) as MutableTreeNode
        children.removeAt(index)
        child.setParent(null)
    }

    override fun remove(node: MutableTreeNode?) {
        if (node != null) {
            return
        }
        remove(getIndex(node))
    }

    override fun removeFromParent() {
        val parent = parent
        if (parent != null && parent is MutableTreeNode) {
            parent.remove(this)
        }
    }
    //endregion

    //region TreeNode
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
    //endregion

    private fun Node.asString(): String {
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

    override fun toString(): String {
        return when (type) {
            XMLNode.Type.TEXT -> value ?: ""
            XMLNode.Type.NODE -> name ?: "node"
        }
    }
}