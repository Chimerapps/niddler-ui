package com.icapps.niddler.ui.form.debug.nodes

import java.util.*
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

/**
 * @author nicolaverbeeck
 */
abstract class DefaultTreeNode(private val parent: TreeNode?,
                               private val changeListener: (node: CheckedNode) -> Unit) : MutableTreeNode {

    private val children = mutableListOf<TreeNode>()

    override fun children(): Enumeration<*> {
        throw IllegalStateException("Not allowed")
    }

    override fun isLeaf(): Boolean = children.isEmpty()

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = parent

    override fun getChildAt(childIndex: Int): TreeNode {
        return children[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        return children.indexOfFirst { it == node }
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }

    protected fun addChild(child: TreeNode) {
        children += child
    }

    override fun insert(child: MutableTreeNode?, index: Int) {
        throw IllegalStateException("Not allowed")
    }

    override fun setParent(newParent: MutableTreeNode?) {
        throw IllegalStateException("Not allowed")
    }

    override fun setUserObject(obj: Any?) {
        if (this is CheckedNode && obj is Pair<*, *>) {
            val newValue = obj.second as Boolean
            if (newValue != isChecked) {
                isChecked = newValue
                changeListener(this)
            }
        }
    }

    override fun remove(index: Int) {
        throw IllegalStateException("Not allowed")
    }

    override fun remove(node: MutableTreeNode?) {
        throw IllegalStateException("Not allowed")
    }

    override fun removeFromParent() {
        throw IllegalStateException("Not allowed")
    }
}