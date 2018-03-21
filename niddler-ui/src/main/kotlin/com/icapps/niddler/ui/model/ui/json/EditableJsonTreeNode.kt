package com.icapps.niddler.ui.model.ui.json

import com.google.gson.JsonElement
import com.icapps.niddler.ui.asEnumeration
import org.apache.http.util.TextUtils
import java.util.*
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class EditableJsonTreeNode(override val jsonElement: JsonElement, private val parent: TreeNode?, override var name: String?) : MutableTreeNode, JsonNode<EditableJsonTreeNode> {

    override var value: String? = null
    override val children: MutableList<EditableJsonTreeNode> = arrayListOf()
    override var type: JsonNode.Type = JsonNode.Type.PRIMITIVE
    override lateinit var primitiveNumber: Number

    init {
        if (this.jsonElement.isJsonArray)
            populateFromArray(this.jsonElement.asJsonArray)
        else if (this.jsonElement.isJsonObject)
            populateFromObject(this.jsonElement.asJsonObject)
        else if (this.jsonElement.isJsonNull)
            initLeaf("null")
        else
            initLeafPrimitive(this.jsonElement.asJsonPrimitive)
    }

    //region JsonNode
    override fun createElement(value: JsonElement, key: String?): EditableJsonTreeNode {
        return EditableJsonTreeNode(value, this, key)
    }
    //endregion

    //region MutableTreeNode
    override fun insert(child: MutableTreeNode?, index: Int) {
        children.add(index, child as EditableJsonTreeNode)
    }

    override fun setParent(newParent: MutableTreeNode?) {

    }

    override fun setUserObject(newObject: Any?) {
        if (newObject is JsonTreeEditor.EditedJson) {
            if (!TextUtils.isEmpty(newObject.key)) {
                var invalid = false
                parent?.children()?.iterator()?.forEach {
                    if (it is JsonNode<*>) {
                        if (it.name == newObject.key && !invalid) {
                            invalid = true
                        }
                    }
                }
                if (!invalid)
                    name = newObject.key
            }
            if (!TextUtils.isEmpty(newObject.value)) {
                value = newObject.value
            }
            type = newObject.type
        }
    }

    override fun remove(index: Int) {
        children.removeAt(index)
    }

    override fun remove(node: MutableTreeNode?) {
        children.remove(node)
    }

    override fun removeFromParent() {
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