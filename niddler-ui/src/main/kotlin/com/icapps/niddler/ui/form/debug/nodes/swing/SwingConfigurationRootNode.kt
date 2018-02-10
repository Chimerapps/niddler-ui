package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode

/**
 * @author nicolaverbeeck
 */
class SwingConfigurationRootNode(changeListener: (node: CheckedNode) -> Unit)
    : SwingDefaultTreeNode(null, changeListener) {

    val delaysRoot: CheckedNode
    val blacklistRoot: SwingBlacklistRootNode

    init {
        delaysRoot = SwingDelaysConfigurationRootNode(false, changeListener)
        blacklistRoot = SwingBlacklistRootNode(false, changeListener)
        addChild(delaysRoot)
        addChild(blacklistRoot)
        addChild(SwingDefaultResponsesNode(false, changeListener))
    }


}