package com.icapps.niddler.ui.form.detail.body

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.icapps.niddler.ui.model.ui.json.JsonNode
import com.icapps.niddler.ui.model.ui.json.JsonTreeRenderer
import com.icapps.niddler.ui.model.ui.json.editor.JsonTreeEditor
import com.icapps.niddler.ui.model.ui.json.editor.JsonTreeEditorNode
import com.icapps.niddler.ui.model.ui.json.editor.JsonTreeEditorTransferHandler
import java.awt.event.KeyEvent
import javax.swing.DropMode
import javax.swing.JTree
import javax.swing.KeyStroke
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Koen Van Looveren
 */
open class NiddlerJsonEditableTree(json: JsonElement) : JTree() {

    init {
        isEditable = true
        dragEnabled = true
        dropMode = DropMode.ON_OR_INSERT
        showsRootHandles = true
        isRootVisible = true
        model = DefaultTreeModel(JsonTreeEditorNode(json, null, null), false)

        setCellEditor(JsonTreeEditor(this))
        setCellRenderer(JsonTreeRenderer())
        transferHandler = JsonTreeEditorTransferHandler()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing")
    }

    fun getEditedJson(): JsonElement {
        val treeNode = (model.root as JsonTreeEditorNode)
        val rootElement = getJsonElement(treeNode)
        getChildren(treeNode, rootElement)
        return rootElement
    }

    private fun getChildren(treeNode: JsonTreeEditorNode, parentElement: JsonElement) {
        treeNode.children.forEach {
            val element = getJsonElement(it)
            when {
                (parentElement is JsonObject) -> {
                    val name = it.name ?: return
                    parentElement.add(name, element)
                }
                (parentElement is JsonArray) -> {
                    parentElement.add(element)
                }
            }
            getChildren(it, element)
        }
    }

    private fun getJsonElement(node: JsonTreeEditorNode): JsonElement {
        return when (node.type) {
            JsonNode.Type.OBJECT -> JsonObject()
            JsonNode.Type.ARRAY -> JsonArray()
            else -> getPrimitiveJson(node.value ?: "")
        }
    }

    private fun getPrimitiveJson(value: String): JsonPrimitive {
        val number = value.toIntOrNull()
        if (number != null) {
            return JsonPrimitive(number)
        }

        val isBoolean = value.equals("true", true) || value.equals("false", true)
        if (isBoolean) {
            return JsonPrimitive(value.toBoolean())
        }

        val charArray = value.toCharArray()
        if (charArray.size == 1) {
            return JsonPrimitive(charArray[0])
        }

        val formattedValue = try {
            value.substring(1, value.length - 1)
        } catch (exception: StringIndexOutOfBoundsException) {
            ""
        }
        return JsonPrimitive(formattedValue)
    }
}