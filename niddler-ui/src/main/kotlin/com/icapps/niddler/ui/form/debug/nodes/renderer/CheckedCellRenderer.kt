package com.icapps.niddler.ui.form.debug.nodes.renderer

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import java.awt.Color
import java.awt.Component
import javax.swing.JTree
import javax.swing.UIManager
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer


/**
 * @author nicolaverbeeck
 */
class CheckedCellRenderer(private val delegate: DefaultTreeCellRenderer) : TreeCellRenderer {

    val panel = CheckedNodePanel()

    private val selectionForeground: Color
    private val selectionBackground: Color
    private val textForeground: Color
    private val textBackground: Color

    init {
        val fontValue = UIManager.getFont("Tree.font")
        if (fontValue != null) panel.text.font = fontValue

        val focusPainted = UIManager.get("Tree.drawsFocusBorderAroundIcon") as Boolean
        panel.checkbox.isFocusPainted = focusPainted

        selectionForeground = UIManager.getColor("Tree.selectionForeground")
        selectionBackground = UIManager.getColor("Tree.selectionBackground")
        textForeground = UIManager.getColor("Tree.textForeground")
        textBackground = UIManager.getColor("Tree.textBackground")
    }

    override fun getTreeCellRendererComponent(tree: JTree,
                                              value: Any?,
                                              selected: Boolean,
                                              expanded: Boolean,
                                              leaf: Boolean,
                                              row: Int,
                                              hasFocus: Boolean): Component {
        if (value !is CheckedNode)
            return delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)

        panel.checkbox.isSelected = value.isChecked
        panel.text.text = value.toString()

        panel.isEnabled = tree.isEnabled

        if (selected) {
            panel.foreground = selectionForeground
            panel.background = selectionBackground
            panel.text.foreground = selectionForeground
            panel.text.background = selectionBackground
        } else {
            panel.foreground = textForeground
            panel.background = textBackground
            panel.text.foreground = textForeground
            panel.text.background = textBackground
        }

        return panel
    }

}