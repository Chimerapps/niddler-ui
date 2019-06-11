package com.chimerapps.niddler.ui.model.renderer.impl.json

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.util.ui.loadIcon
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBFont
import java.awt.Font
import java.awt.Point
import java.awt.event.MouseEvent
import java.util.Enumeration
import javax.swing.JComponent
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel

class JsonBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = true
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val data = (message.bodyData as? JsonElement) ?: JsonPrimitive("")
        if (reuseComponent is JBScrollPane && reuseComponent.componentCount != 0 && reuseComponent.getComponent(0) is NiddlerJsonTree) {
            return (reuseComponent.getComponent(0) as NiddlerJsonTree).also { it.resetModel(data) }
        }
        return JBScrollPane(NiddlerJsonTree(data))
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val stringData = GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(message.bodyData)

        return textArea(stringData, reuseComponent)
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val stringData = message.message.getBodyAsString(message.bodyFormat.encoding) ?: ""
        return textArea(stringData, reuseComponent)
    }

    private fun textArea(stringData: String, reuseComponent: JComponent?): JComponent {
        val component = if (reuseComponent is JBScrollPane && reuseComponent.componentCount != 0 && reuseComponent.getComponent(0) is JTextArea) {
            reuseComponent to reuseComponent.getComponent(0) as JTextArea
        } else {
            val textArea = JTextArea().also {
                it.isEditable = false
                it.font = JBFont.create(Font("Monospaced", 0, 10))
            }
            JBScrollPane(textArea) to textArea
        }

        val doc = component.second.document
        doc.remove(0, doc.length)
        doc.insertString(0, stringData, null)
        return component.first
    }

}

private class NiddlerJsonTree(json: JsonElement) : Tree() {

    //var popup: NiddlerStructuredViewPopupMenu? = null

    init {
        isEditable = false
        setShowsRootHandles(true)
        isRootVisible = true
        resetModel(json)

        setCellRenderer(JsonTreeCellRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }

    fun resetModel(json: JsonElement) {
        model = DefaultTreeModel(JsonTreeNode(json, null, null), false)
    }

    override fun getPopupLocation(event: MouseEvent): Point? {
        val path = getClosestPathForLocation(event.x, event.y)
        clearSelection()
        selectionModel.selectionPath = path
        return super.getPopupLocation(event)
    }

//    override fun getComponentPopupMenu(): JPopupMenu? {
//        val path = selectionPath ?: return null
//        val popup = popup ?: return null
//
//        val node = path.lastPathComponent as JsonTreeNode
//        if (node.isLeaf) {
//            popup.init(key = node.name, value = node.value)
//        } else {
//            val value = GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(node.jsonElement)
//            popup.init(key = node.name, value = value)
//        }
//
//        return popup
//    }
}


private class JsonTreeNode(override val jsonElement: JsonElement, private val parent: TreeNode?, override val name: String?) : TreeNode, JsonNode<JsonTreeNode> {

    override val children: MutableList<JsonTreeNode> = arrayListOf()
    override var value: String? = null
    override var type: JsonNode.Type = JsonNode.Type.PRIMITIVE
    override lateinit var primitiveNumber: Number

    init {
        when {
            jsonElement.isJsonArray -> populateFromArray(jsonElement.asJsonArray)
            jsonElement.isJsonObject -> populateFromObject(jsonElement.asJsonObject)
            jsonElement.isJsonNull -> initLeaf("null")
            else -> initLeafPrimitive(jsonElement.asJsonPrimitive)
        }
    }

    override fun createElement(value: JsonElement, key: String?): JsonTreeNode {
        return JsonTreeNode(value, this, key)
    }

    override fun children(): Enumeration<*> = object : Enumeration<JsonTreeNode> {

        private val it = children.iterator()

        override fun hasMoreElements(): Boolean = it.hasNext()

        override fun nextElement(): JsonTreeNode = it.next()

    }

    override fun isLeaf(): Boolean = children.isEmpty()

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = parent

    override fun getChildAt(childIndex: Int): TreeNode = children[childIndex]

    override fun getIndex(node: TreeNode?): Int = children.indexOf(node)

    override fun getAllowsChildren(): Boolean = true //No idea?

    override fun toString(): String {
        return when (type) {
            JsonNode.Type.ARRAY -> if (name != null) "$name[$childCount]" else "array[$childCount]"
            JsonNode.Type.OBJECT -> name ?: "object"
            JsonNode.Type.PRIMITIVE -> if (name != null) "$name : $value" else "$value"
        }
    }
}

private class JsonTreeCellRenderer : ColoredTreeCellRenderer() {

    private val booleanIcon = loadIcon("/ic_boolean.png")
    private val intIcon = loadIcon("/ic_int.png")
    private val stringIcon = loadIcon("/ic_string.png")
    private val doubleIcon = loadIcon("/ic_double.png")

    override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        if (value !is JsonNode<*>)
            return

        var useItalic = false

        icon = when (value.actualType()) {
            JsonNode.JsonDataType.ARRAY -> {
                useItalic = value.isAnonymous()
                AllIcons.Json.Array
            }
            JsonNode.JsonDataType.OBJECT -> {
                useItalic = value.isAnonymous()
                AllIcons.Json.Object
            }
            JsonNode.JsonDataType.BOOLEAN -> booleanIcon
            JsonNode.JsonDataType.INT -> intIcon
            JsonNode.JsonDataType.STRING -> stringIcon
            JsonNode.JsonDataType.DOUBLE -> doubleIcon
            JsonNode.JsonDataType.NULL -> null
        }

        if (useItalic)
            append(value.toString(), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES)
        else
            append(value.toString())
    }

}