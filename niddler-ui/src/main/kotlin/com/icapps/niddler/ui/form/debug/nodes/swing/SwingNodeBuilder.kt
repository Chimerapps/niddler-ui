package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.*

/**
 * @author nicolaverbeeck
 */
class SwingNodeBuilder(private val changeListener: (node: CheckedNode) -> Unit) : NodeBuilder {

    override fun createBlacklistRootNode(isChecked: Boolean): BlacklistRootNode {
        return SwingBlacklistRootNode(isChecked, changeListener)
    }

    override fun createDelaysConfigurationNode(isChecked: Boolean): DelaysConfigurationRootNode {
        return SwingDelaysConfigurationRootNode(isChecked, changeListener)
    }

    override fun createBlacklistNode(regex: String, isChecked: Boolean): BlacklistItemNode {
        return SwingBlacklistNode(regex, isChecked, changeListener)
    }

    override fun createDefaultResponseRootNode(isChecked: Boolean): DefaultResponseRootNode {
        return SwingDefaultResponseRootNode(isChecked, changeListener)
    }

    override fun createRootNode(): ConfigurationRootNode {
        return SwingConfigurationRootNode(changeListener)
    }


}