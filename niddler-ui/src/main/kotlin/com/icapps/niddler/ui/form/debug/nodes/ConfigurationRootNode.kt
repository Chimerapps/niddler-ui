package com.icapps.niddler.ui.form.debug.nodes

/**
 * @author nicolaverbeeck
 */
class ConfigurationRootNode(changeListener: () -> Unit) : DefaultTreeNode(null, changeListener) {

    val delaysRoot: CheckedNode
    val blacklistRoot: BlacklistRootNode

    init {
        delaysRoot = TimeoutConfigurationRootNode(this, changeListener)
        blacklistRoot = BlacklistRootNode(this, changeListener)
        addChild(delaysRoot)
        addChild(blacklistRoot)
        addChild(DefaultResponsesNode(this, changeListener))
    }


}