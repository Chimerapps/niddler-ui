package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.DefaultResponseRootNode

/**
 * @author nicolaverbeeck
 */
class SwingDefaultResponseRootNode(isChecked: Boolean, changeListener: (node: CheckedNode) -> Unit)
    : DefaultResponseRootNode, SwingCheckedNode("Default responses", isChecked, changeListener) {

    override fun getAllowsChildren(): Boolean = true

}