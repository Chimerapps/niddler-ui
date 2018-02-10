package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.DelaysConfigurationRootNode

/**
 * @author nicolaverbeeck
 */
class SwingDelaysConfigurationRootNode(enabled: Boolean,
                                       changeListener: (node: CheckedNode) -> Unit)
    : DelaysConfigurationRootNode, SwingCheckedNode("Delay configuration", enabled, changeListener) {

    override fun getAllowsChildren(): Boolean {
        return false
    }
}