package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import org.scijava.swing.checkboxtree.CheckBoxNodeData

/**
 * @author nicolaverbeeck
 */
class SwingBlacklistNode(regex: String, enabled: Boolean,
                         changeListener: (node: CheckedNode) -> Unit)
    : SwingCheckedNode(regex, enabled, changeListener) {

    val regex: String
        get() = (userObject as CheckBoxNodeData).text

    fun updateRegex(regex: String) {
        (userObject as CheckBoxNodeData).text
    }

    override fun getAllowsChildren(): Boolean {
        return false
    }
}