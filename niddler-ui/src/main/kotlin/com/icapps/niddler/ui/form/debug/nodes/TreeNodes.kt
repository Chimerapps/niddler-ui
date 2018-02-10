package com.icapps.niddler.ui.form.debug.nodes

/**
 * @author nicolaverbeeck
 */
interface TreeNode {
    fun addChild(node: TreeNode)
}

interface CheckedNode : TreeNode {

    var nodeCheckState: Boolean

}

interface BlacklistRootNode : CheckedNode {

    fun addBlacklistItem(regex: String): BlacklistItemNode

    fun isEnabled(regex: String): Boolean

}

interface DelaysConfigurationRootNode : CheckedNode

interface BlacklistItemNode : CheckedNode {

    val regex: String

    fun updateRegex(regex: String)

}

interface DefaultResponseRootNode : CheckedNode

interface ConfigurationRootNode : TreeNode {

    val delaysRoot: DelaysConfigurationRootNode

    val blacklistRoot: BlacklistRootNode

}

interface NodeBuilder {

    fun createBlacklistRootNode(isChecked: Boolean): BlacklistRootNode

    fun createDelaysConfigurationNode(isChecked: Boolean): DelaysConfigurationRootNode

    fun createBlacklistNode(regex: String, isChecked: Boolean): BlacklistItemNode

    fun createDefaultResponseRootNode(isChecked: Boolean): DefaultResponseRootNode

    fun createRootNode(): ConfigurationRootNode

}