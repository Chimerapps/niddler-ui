package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration
import com.icapps.niddler.ui.form.debug.nodes.BlacklistRootNode
import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationRootNode
import com.icapps.niddler.ui.form.debug.nodes.DelaysConfigurationRootNode

/**
 * @author nicolaverbeeck
 */
class SwingConfigurationRootNode(configuration: TemporaryDebuggerConfiguration,
                                 changeListener: (node: CheckedNode) -> Unit)
    : ConfigurationRootNode, SwingDefaultTreeNode(null, changeListener) {

    override val delaysRoot: DelaysConfigurationRootNode
    override val blacklistRoot: BlacklistRootNode

    init {
        delaysRoot = SwingDelaysConfigurationRootNode(false, changeListener)
        blacklistRoot = SwingBlacklistRootNode(false, configuration, changeListener)
        addChild(delaysRoot)
        addChild(blacklistRoot)
        addChild(SwingDefaultResponseRootNode(false, changeListener))
    }


}