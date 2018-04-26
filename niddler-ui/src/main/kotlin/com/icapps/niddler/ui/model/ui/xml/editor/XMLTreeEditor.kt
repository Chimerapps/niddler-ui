package com.icapps.niddler.ui.model.ui.xml.editor

import com.icapps.niddler.ui.model.ui.xml.XMLNode
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.tree.TreeCellEditor

/**
 * @author Koen Van Looveren
 */
class XMLTreeEditor(val tree: JTree) : AbstractCellEditor(), TreeCellEditor {

    private val stringIcon: Icon
    private val nodeIcon: Icon

    private var regularFont: Font

    private var editType: XMLNode.Type = XMLNode.Type.NODE

    private val keyField = JTextField()
    private val keyLabel = JLabel()

    init {
        stringIcon = ImageIcon(javaClass.getResource("/string.png"))
        nodeIcon = ImageIcon(javaClass.getResource("/ic_xml_node.png"))

        regularFont = Font("Monospaced", 0, 11)
        keyField.font = regularFont
        keyLabel.font = regularFont

        keyField.addActionListener { fireEditingStopped() }
    }

    override fun getCellEditorValue(): Any {
        return when (editType) {
            XMLNode.Type.TEXT -> EditedXML(keyField.text.trim(), editType)
            XMLNode.Type.NODE -> EditedNodeXML(keyField.text.trim(), editType)
        }
    }

    override fun getTreeCellEditorComponent(tree: JTree?, value: Any?, isSelected: Boolean, expanded: Boolean, leaf: Boolean, row: Int): Component {
        val panel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0))
        panel.background = Color.WHITE

        keyField.text = ""
        if (value is XMLNode<*>) {
            val icon = JLabel(getCorrectIconRes(value.type))
            panel.add(icon)
            when (value.type) {
                XMLNode.Type.TEXT -> {
                    editType = XMLNode.Type.TEXT
                    keyField.text = value.value
                    panel.add(keyField)
                }
                XMLNode.Type.NODE -> {
                    editType = XMLNode.Type.NODE
                    keyField.text = value.name
                    panel.add(keyField)
                }
            }
        } else {
            keyLabel.text = value.toString()
            panel.add(keyLabel)
        }
        return panel
    }


    private fun getCorrectIconRes(actualType: XMLNode.Type): Icon? {
        return when (actualType) {
            XMLNode.Type.NODE -> nodeIcon
            XMLNode.Type.TEXT -> stringIcon
        }
    }

    data class EditedXML(val name: String?, val type: XMLNode.Type)

    data class EditedNodeXML(val value: String?, val type: XMLNode.Type)
}