package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationRootNode
import javax.swing.tree.DefaultTreeModel

/**
 * @author nicolaverbeeck
 */
class ConfigurationModel(changeListener: (node: CheckedNode) -> Unit) {

    val treeModel: DefaultTreeModel
    val root: ConfigurationRootNode = ConfigurationRootNode(changeListener)

    init {
        treeModel = DefaultTreeModel(root, true)
    }

    fun isDelaysEnabled(): Boolean {
        return root.delaysRoot.isChecked
    }

    fun isBlacklistEnabled(regex: String?): Boolean {
        if (regex == null)
            return false

        return root.blacklistRoot.isEnabled(regex)
    }

    fun setDelaysEnabled(enabled: Boolean) {
        root.delaysRoot.isChecked = enabled
        treeModel.nodeChanged(root.delaysRoot)
    }
}