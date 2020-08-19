package com.chimerapps.niddler.ui.model.renderer.impl.json

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.textAreaRenderer
import com.chimerapps.niddler.ui.util.localization.Tr
import com.chimerapps.niddler.ui.util.ui.ClipboardUtil
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.chimerapps.niddler.ui.util.ui.Popup
import com.chimerapps.niddler.ui.util.ui.PopupAction
import com.chimerapps.niddler.ui.util.ui.action
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.icons.AllIcons
import com.intellij.json.JsonFileType
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBFont
import java.awt.Font
import java.awt.Point
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent
import java.util.Enumeration
import javax.swing.JComponent
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel

object JsonBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = true
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        val data = (message.bodyData as? JsonElement) ?: JsonPrimitive("")
        if (reuseComponent is JBScrollPane && reuseComponent.componentCount != 0 && reuseComponent.getComponent(0) is NiddlerJsonTree) {
            return (reuseComponent.getComponent(0) as NiddlerJsonTree).also { it.resetModel(data) }
        }
        return JBScrollPane(NiddlerJsonTree(data).also { TreeSpeedSearch(it, { path -> path.lastPathComponent.toString() }, true) })
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        return textAreaRenderer(prettyText(message.bodyData), reuseComponent, project, JsonFileType.INSTANCE, requestFocus)
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        val stringData = message.message.getBodyAsString(message.bodyFormat.encoding) ?: ""
        return textAreaRenderer(stringData, reuseComponent, project, JsonFileType.INSTANCE, requestFocus)
    }

    override fun prettyText(bodyData: Any?): String {
        return GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(bodyData)
    }
}

private class NiddlerJsonTree(json: JsonElement) : Tree() {

    init {
        isEditable = false
        setShowsRootHandles(true)
        isRootVisible = true
        resetModel(json)

        setCellRenderer(JsonTreeCellRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        transferHandler = JsonTreeTransferHandler(transferHandler)
    }

    fun resetModel(json: JsonElement) {
        model = DefaultTreeModel(JsonTreeNode(json, parent = null, name = null, index = null), false)
    }

    override fun getPopupLocation(event: MouseEvent): Point? {
        val path = getClosestPathForLocation(event.x, event.y)
        clearSelection()
        selectionModel.selectionPath = path
        return super.getPopupLocation(event)
    }

    override fun getComponentPopupMenu(): JPopupMenu? {
        val path = selectionPath ?: return null

        val node = path.lastPathComponent as JsonTreeNode
        val actions = mutableListOf<PopupAction>()
        actions += Tr.ViewJsonActionCopyJson.tr() action {
            ClipboardUtil.copyToClipboard(StringSelection(JsonTreeTransferHandler.formatJson(node.jsonElement)))
        }
        node.name?.let { actions += Tr.ViewActionCopyKey.tr() action { ClipboardUtil.copyToClipboard(StringSelection(it)) } }
        node.value?.let { actions += Tr.ViewActionCopyValue.tr() action { ClipboardUtil.copyToClipboard(StringSelection(it)) } }

        return Popup(actions)
    }
}


private class JsonTreeNode(override val jsonElement: JsonElement, private val parent: TreeNode?,
                           override val name: String?, private val index: Int?) : TreeNode, JsonNode<JsonTreeNode> {

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

    override fun createElement(value: JsonElement, key: String?, index: Int?): JsonTreeNode {
        return JsonTreeNode(value, this, key, index)
    }

    override fun children(): Enumeration<out TreeNode>? = object : Enumeration<JsonTreeNode> {

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
        val infix = if (index != null) "[$index] " else ""

        return when (type) {
            JsonNode.Type.ARRAY -> if (name != null) "$name$infix[$childCount]" else "array$infix[$childCount]"
            JsonNode.Type.OBJECT -> "${name ?: "object"}$infix"
            JsonNode.Type.PRIMITIVE -> if (name != null) "$infix$name : $value" else "$infix$value"
        }
    }
}

private class JsonTreeCellRenderer : ColoredTreeCellRenderer() {

    private val booleanIcon = IncludedIcons.Types.boolean
    private val intIcon = IncludedIcons.Types.int
    private val stringIcon = IncludedIcons.Types.string
    private val doubleIcon = IncludedIcons.Types.double
    private val monoSpaced = JBFont.create(Font("Monospaced", 0, 12))

    override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        if (value !is JsonNode<*>)
            return

        font = monoSpaced
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