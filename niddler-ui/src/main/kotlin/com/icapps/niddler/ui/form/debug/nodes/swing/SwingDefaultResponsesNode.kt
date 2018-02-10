package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode

/**
 * @author nicolaverbeeck
 */
class SwingDefaultResponsesNode(isChecked: Boolean, changeListener: (node: CheckedNode) -> Unit)
    : SwingCheckedNode("Default responses", isChecked, changeListener) {

    override fun getAllowsChildren(): Boolean = true

}