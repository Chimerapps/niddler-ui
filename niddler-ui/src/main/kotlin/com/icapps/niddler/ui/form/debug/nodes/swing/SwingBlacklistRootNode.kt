package com.icapps.niddler.ui.form.debug.nodes.swing

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode

/**
 * @author nicolaverbeeck
 */
class SwingBlacklistRootNode(isChecked: Boolean,
                             private val changeListener: (node: CheckedNode) -> Unit)
    : SwingCheckedNode("Blacklist", isChecked, changeListener) {

    private val nodes: MutableList<SwingBlacklistNode> = mutableListOf()

    fun addBlacklistItem(regex: String) {
        val node = SwingBlacklistNode(regex, true, changeListener)
        nodes.add(node)
        addChild(node)
    }

    fun isEnabled(regex: String): Boolean {
        return nodes.find { it.regex == regex }?.nodeCheckState == true
    }

    override fun getAllowsChildren(): Boolean = true

}