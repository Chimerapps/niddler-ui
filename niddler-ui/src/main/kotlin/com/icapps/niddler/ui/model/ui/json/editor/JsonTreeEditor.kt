package com.icapps.niddler.ui.model.ui.json.editor

import com.icapps.niddler.ui.model.ui.json.JsonNode
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.tree.TreeCellEditor

/**
 * @author Koen Van Looveren
 */
class JsonTreeEditor(var tree: JTree) : AbstractCellEditor(), TreeCellEditor {

    private val stringIcon: Icon
    private val booleanIcon: Icon
    private val intIcon: Icon
    private val objectIcon: Icon
    private val arrayIcon: Icon
    private val doubleIcon: Icon

    private var italicFont: Font
    private var regularFont: Font

    private val keyField = JTextField()
    private val valueField = JTextField()
    private val separatorLabel = JLabel()
    private val keyLabel = JLabel()
    private var editType: JsonNode.Type = JsonNode.Type.PRIMITIVE

    init {
        stringIcon = ImageIcon(javaClass.getResource("/string.png"))
        booleanIcon = ImageIcon(javaClass.getResource("/boolean.png"))
        intIcon = ImageIcon(javaClass.getResource("/int.png"))
        objectIcon = ImageIcon(javaClass.getResource("/object.png"))
        arrayIcon = ImageIcon(javaClass.getResource("/array.png"))
        doubleIcon = ImageIcon(javaClass.getResource("/double.png"))

        italicFont = Font("Monospaced", Font.ITALIC, 11)
        regularFont = Font("Monospaced", 0, 11)

        keyLabel.font = regularFont
        keyLabel.border = EmptyBorder(0, 2, 0, 0)
        keyField.font = regularFont
        separatorLabel.font = regularFont
        valueField.font = regularFont
        keyField.addActionListener { fireEditingStopped() }
        valueField.addActionListener { fireEditingStopped() }
    }

    override fun getCellEditorValue(): Any {
        return when (editType) {
            JsonNode.Type.PRIMITIVE -> {
                var field = valueField.text
                val isString = when {
                    field.toLongOrNull() != null -> false
                    field.toFloatOrNull() != null -> false
                    field.equals("true", true) || field.equals("false", true) -> false
                    else -> true
                }
                if (isString) {
                    if (!field.endsWith("\""))
                        field += "\""
                    if (!field.startsWith("\""))
                        field = "\"" + field
                    return EditedJson(keyField.text.trim(), field, editType)
                }
                return EditedJson(keyField.text.trim(), valueField.text.trim(), editType)
            }
            JsonNode.Type.OBJECT -> EditedJson(keyField.text.trim(), null, editType)
            JsonNode.Type.ARRAY -> EditedJson(keyField.text.trim(), null, editType)
        }
    }

    override fun getTreeCellEditorComponent(tree: JTree?, value: Any?, isSelected: Boolean, expanded: Boolean, leaf: Boolean, row: Int): Component {
        val panel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0))
        panel.background = Color.WHITE

        keyField.text = ""
        valueField.text = ""
        keyLabel.text = ""
        if (value is JsonNode<*>) {
            val icon = JLabel(getCorrectIconRes(value.actualType()))
            panel.add(icon)
            when (value.actualType()) {
                JsonNode.JsonDataType.OBJECT -> {
                    editType = JsonNode.Type.OBJECT
                    if (value.isAnonymous()) {
                        keyLabel.font = italicFont
                        keyLabel.text = value.toString()
                        panel.add(keyLabel)
                    } else {
                        keyField.font = regularFont
                        keyField.text = value.name
                        panel.add(keyField)
                    }
                }
                JsonNode.JsonDataType.ARRAY -> {
                    editType = JsonNode.Type.ARRAY
                    if (value.isAnonymous()) {
                        keyLabel.font = italicFont
                        keyLabel.text = value.toString()
                        panel.add(keyLabel)
                    } else {
                        keyField.font = regularFont
                        keyField.text = value.name
                        panel.add(keyField)
                    }
                }
                else -> {
                    keyField.font = if (value.isAnonymous()) italicFont else regularFont
                    editType = JsonNode.Type.PRIMITIVE
                    keyField.text = value.name
                    panel.add(keyField)

                    separatorLabel.text = " : "
                    panel.add(separatorLabel)

                    valueField.text = value.value
                    panel.add(valueField)
                }
            }
        } else {
            keyLabel.text = value.toString()
            panel.add(keyLabel)
        }
        return panel
    }

    private fun getCorrectIconRes(actualType: JsonNode.JsonDataType): Icon? {
        return when (actualType) {
            JsonNode.JsonDataType.ARRAY -> arrayIcon
            JsonNode.JsonDataType.OBJECT -> objectIcon
            JsonNode.JsonDataType.BOOLEAN -> booleanIcon
            JsonNode.JsonDataType.INT -> intIcon
            JsonNode.JsonDataType.STRING -> stringIcon
            JsonNode.JsonDataType.DOUBLE -> doubleIcon
            JsonNode.JsonDataType.NULL -> null
        }
    }

    data class EditedJson(val key: String, val value: String?, val type: JsonNode.Type)
}