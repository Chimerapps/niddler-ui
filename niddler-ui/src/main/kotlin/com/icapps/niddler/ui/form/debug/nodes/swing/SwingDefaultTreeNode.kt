package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationNode
import com.icapps.niddler.ui.form.debug.nodes.TreeNode
import org.scijava.swing.checkboxtree.CheckBoxNodeData
import javax.swing.tree.DefaultMutableTreeNode

/**
 * @author nicolaverbeeck
 */
open class SwingDefaultTreeNode(userData: Any?,
                                override val configurationNode: ConfigurationNode<*>,
                                private val changeListener: (node: CheckedNode) -> Unit)
    : TreeNode, DefaultMutableTreeNode(userData) {

    override fun setCanHaveChildren(canHaveChildren: Boolean) {
        setAllowsChildren(canHaveChildren)
    }

    override fun updateText(text: String) {
        userObject = text
    }

    override fun text(): String {
        return toString()
    }

    override fun addChild(node: TreeNode) {
        if (node is SwingDefaultTreeNode)
            add(node)
        else
            throw IllegalStateException("Wrong type!")
    }

    override fun removeChild(node: TreeNode) {
        if (node is SwingDefaultTreeNode)
            remove(node)
        else
            throw IllegalStateException("Wrong type!")
    }

    override fun setUserObject(obj: Any?) {
        if (this is CheckedNode && obj is CheckBoxNodeData) {
            val newValue = obj.isChecked
            if (newValue != nodeCheckState) {
                nodeCheckState = newValue
                changeListener(this)
            }
        }
    }

}

open class SwingCheckedNode(text: String,
                            isChecked: Boolean,
                            configurationNode: ConfigurationNode<*>,
                            changeListener: (node: CheckedNode) -> Unit)
    : CheckedNode, SwingDefaultTreeNode(CheckBoxNodeData(text, isChecked), configurationNode, changeListener) {

    override fun updateText(text: String) {
        (userObject as CheckBoxNodeData).text = text
    }

    override var nodeCheckState: Boolean
        get() = (userObject as CheckBoxNodeData).isChecked
        set(value) {
            (userObject as CheckBoxNodeData).isChecked = value
        }

    override fun text(): String {
        return (userObject as CheckBoxNodeData).text
    }
}
