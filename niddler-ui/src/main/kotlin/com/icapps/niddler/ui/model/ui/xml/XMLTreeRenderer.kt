package com.icapps.niddler.ui.model.ui.xml

import com.icapps.niddler.ui.util.loadIcon
import java.awt.Component
import java.awt.Font
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class XMLTreeRenderer : DefaultTreeCellRenderer() {

    private val stringIcon: Icon
    private val nodeIcon: Icon

    private var regularFont: Font

    init {
        stringIcon = loadIcon("/string.png")
        nodeIcon = loadIcon("/ic_xml_node.png")

        regularFont = Font("Monospaced", 0, 11)
    }

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        icon = null
        font = regularFont
        if (value is XMLTreeNode) {
            when (value.type) {
                XMLTreeNode.Type.NODE -> {
                    icon = nodeIcon
                }
                XMLTreeNode.Type.TEXT -> {
                    icon = stringIcon
                }
            }
        }
        return this
    }

}