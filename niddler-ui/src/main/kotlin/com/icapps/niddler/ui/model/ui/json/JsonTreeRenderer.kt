package com.icapps.niddler.ui.model.ui.json

import java.awt.Component
import java.awt.Font
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class JsonTreeRenderer : DefaultTreeCellRenderer() {

    private val stringIcon: Icon
    private val booleanIcon: Icon
    private val intIcon: Icon
    private val objectIcon: Icon
    private val arrayIcon: Icon
    private val doubleIcon: Icon

    private var italicFont: Font
    private var regularFont: Font

    init {
        stringIcon = ImageIcon(javaClass.getResource("/string.png"))
        booleanIcon = ImageIcon(javaClass.getResource("/boolean.png"))
        intIcon = ImageIcon(javaClass.getResource("/int.png"))
        objectIcon = ImageIcon(javaClass.getResource("/object.png"))
        arrayIcon = ImageIcon(javaClass.getResource("/array.png"))
        doubleIcon = ImageIcon(javaClass.getResource("/double.png"))

        italicFont = Font("Monospaced", Font.ITALIC, 11)
        regularFont = Font("Monospaced", 0, 11)
    }

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
        if (value is JsonNode<*>) {
            font = regularFont
            text = value.toString()
            icon = when (value.actualType()) {
                JsonNode.JsonDataType.ARRAY -> {
                    if (value.isAnonymous())
                        font = italicFont
                    arrayIcon
                }
                JsonNode.JsonDataType.OBJECT -> {
                    if (value.isAnonymous())
                        font = italicFont
                    objectIcon
                }
                JsonNode.JsonDataType.BOOLEAN -> booleanIcon
                JsonNode.JsonDataType.INT -> intIcon
                JsonNode.JsonDataType.STRING -> stringIcon
                JsonNode.JsonDataType.DOUBLE -> doubleIcon
                JsonNode.JsonDataType.NULL -> null
            }
        }
        return this
    }
}