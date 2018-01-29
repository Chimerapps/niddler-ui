package com.icapps.niddler.ui.form.debug.nodes

/**
 * @author nicolaverbeeck
 */
class ConfigurationRootNode : DefaultTreeNode(null) {

    init {
        addChild(BlacklistRootNode(this))
        addChild(DefaultResponsesNode(this))
    }

}