package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.ui.debugger.model.saved.DisableableItem
import com.icapps.niddler.ui.form.debug.ConfigurationModel
import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationNode
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationNodeWithChildren

/**
 * @author nicolaverbeeck
 */
class DebuggerConfigurationModelAdapter(
        private val debuggerConfiguration: DebuggerConfiguration,
        private val configurationModel: ConfigurationModel) {

    private val root = configurationModel.configurationRoot

    fun syncNodes() {
        updateCheckState(root.delaysRoot, "Delays", debuggerConfiguration.delayConfiguration)

        updateList(root.blacklistRoot, "Blacklist", debuggerConfiguration.blacklistConfiguration)
    }

    private fun <T> updateCheckState(node: ConfigurationNode<T>, text: String, disableableItem: DisableableItem<T>) {
        updateCheckState(node, text, disableableItem.enabled)
        node.nodeData = disableableItem.item
    }

    private fun updateCheckState(node: ConfigurationNode<*>, text: String, state: Boolean) {
        val checkedNode = (node.treeNode as CheckedNode)
        if (checkedNode.nodeCheckState == state && checkedNode.text() == text)
            return
        checkedNode.nodeCheckState = state
        checkedNode.updateText(text)
        configurationModel.nodeChanged(checkedNode)
    }

    private fun <T> updateList(rootNode: ConfigurationNodeWithChildren<ConfigurationNode<T>, *>,
                               text: String, items: List<DisableableItem<T>>) {
        val enabledCount = items.count { it.enabled }

        updateCheckState(rootNode, text, enabledCount == items.size)
        while (rootNode.childCount > items.size) {
            rootNode.popNode()
        }
        while (rootNode.childCount < items.size) {
            rootNode.pushNode()
        }
        rootNode.forEachNode { index, item ->
            val disableableItem = items[index]
            updateCheckState(item, disableableItem.item.toString(), disableableItem)
        }
    }

}