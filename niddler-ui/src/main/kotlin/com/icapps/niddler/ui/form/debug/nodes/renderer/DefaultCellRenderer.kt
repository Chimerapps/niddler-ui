package com.icapps.niddler.ui.form.debug.nodes.renderer

import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author nicolaverbeeck
 */
class DefaultCellRenderer : DefaultTreeCellRenderer() {

    override fun getTreeCellRendererComponent(tree: JTree?,
                                              value: Any?,
                                              selected: Boolean,
                                              expanded: Boolean,
                                              leaf: Boolean,
                                              row: Int,
                                              hasFocus: Boolean): Component {
        return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
    }

}