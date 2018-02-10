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

interface BlacklistRootNode : CheckedNode

interface DelaysConfigurationRootNode : CheckedNode

interface BlacklistItemNode : CheckedNode

interface DefaultResponsesNode : CheckedNode