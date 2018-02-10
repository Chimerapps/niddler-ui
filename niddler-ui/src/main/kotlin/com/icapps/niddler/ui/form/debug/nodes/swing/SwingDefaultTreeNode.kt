package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.TreeNode
import org.scijava.swing.checkboxtree.CheckBoxNodeData
import javax.swing.tree.DefaultMutableTreeNode

/**
 * @author nicolaverbeeck
 */
abstract class SwingDefaultTreeNode(userData: Any?,
                                    private val changeListener: (node: CheckedNode) -> Unit)
    : TreeNode, DefaultMutableTreeNode(userData) {

    override fun addChild(node: TreeNode) {
        if (node is SwingDefaultTreeNode)
            add(node)
        else
            throw IllegalStateException("Wrong type!")
    }

    override fun setUserObject(obj: Any?) {
        if (this is CheckedNode && obj is CheckBoxNodeData) {
            val newValue = obj.isChecked as Boolean
            if (newValue != nodeCheckState) {
                nodeCheckState = newValue
                changeListener(this)
            }
        }
    }

}

open class SwingCheckedNode(text: String,
                            isChecked: Boolean,
                            changeListener: (node: CheckedNode) -> Unit)
    : CheckedNode, SwingDefaultTreeNode(CheckBoxNodeData(text, isChecked), changeListener) {

    override var nodeCheckState: Boolean
        get() = (userObject as CheckBoxNodeData).isChecked
        set(value) {
            (userObject as CheckBoxNodeData).isChecked = value
        }
}
