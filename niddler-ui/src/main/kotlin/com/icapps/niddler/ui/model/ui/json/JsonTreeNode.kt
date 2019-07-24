package com.icapps.niddler.ui.model.ui.json

import com.google.gson.JsonElement
import com.icapps.niddler.ui.asEnumeration
import java.util.*
import javax.swing.tree.TreeNode

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class JsonTreeNode(override val jsonElement: JsonElement, private val parent: TreeNode?, override val name: String?) : TreeNode, JsonNode<JsonTreeNode> {

    override val children: MutableList<JsonTreeNode> = arrayListOf()
    override var value: String? = null
    override var type: JsonNode.Type = JsonNode.Type.PRIMITIVE
    override lateinit var primitiveNumber: Number

    init {
        if (jsonElement.isJsonArray)
            populateFromArray(jsonElement.asJsonArray)
        else if (jsonElement.isJsonObject)
            populateFromObject(jsonElement.asJsonObject)
        else if (jsonElement.isJsonNull)
            initLeaf("null")
        else
            initLeafPrimitive(jsonElement.asJsonPrimitive)
    }

    //region JsonNode
    override fun createElement(value: JsonElement, key: String?): JsonTreeNode {
        return JsonTreeNode(value, this, key)
    }
    //endregion

    //region TreeNode
    override fun children(): Enumeration<*> {
        return children.iterator().asEnumeration()
    }

    override fun isLeaf(): Boolean {
        return children.isEmpty()
    }

    override fun getChildCount(): Int {
        return children.size
    }

    override fun getParent(): TreeNode? {
        return parent
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        return children[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        return children.indexOf(node)
    }

    override fun getAllowsChildren(): Boolean {
        return true //No idea?
    }
    //endregion

    override fun toString(): String {
        return when (type) {
            JsonNode.Type.ARRAY -> if (name != null) "$name[$childCount]" else "array[$childCount]"
            JsonNode.Type.OBJECT -> name ?: "object"
            JsonNode.Type.PRIMITIVE -> if (name != null) "$name : $value" else "$value"
        }
    }
}