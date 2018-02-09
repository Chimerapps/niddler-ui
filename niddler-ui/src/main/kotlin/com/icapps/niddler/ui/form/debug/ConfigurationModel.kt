package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.form.debug.nodes.ConfigurationRootNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/**
 * @author nicolaverbeeck
 */
class ConfigurationModel(changeListener: () -> Unit) {

    val treeModel: TreeModel
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
    }
}