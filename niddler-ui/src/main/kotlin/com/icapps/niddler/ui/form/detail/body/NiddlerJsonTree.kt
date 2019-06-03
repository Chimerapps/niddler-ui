package com.icapps.niddler.ui.form.detail.body

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.icapps.niddler.ui.form.NiddlerStructuredViewPopupMenu
import com.icapps.niddler.ui.model.ui.json.JsonTreeNode
import com.icapps.niddler.ui.model.ui.json.JsonTreeRenderer
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel


/**
 * @author Koen Van Looveren
 */
class NiddlerJsonTree(json: JsonElement) : JTree() {

    var popup: NiddlerStructuredViewPopupMenu? = null

    init {
        isEditable = false
        setShowsRootHandles(true)
        isRootVisible = true
        model = DefaultTreeModel(JsonTreeNode(json, null, null), false)

        setCellRenderer(JsonTreeRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }

    override fun getPopupLocation(event: MouseEvent): Point? {
        val path = getClosestPathForLocation(event.x, event.y)
        clearSelection()
        selectionModel.selectionPath = path
        return super.getPopupLocation(event)
    }

    override fun getComponentPopupMenu(): JPopupMenu? {
        val path = selectionPath ?: return null
        val popup = popup ?: return null

        val node = path.lastPathComponent as JsonTreeNode
        if (node.isLeaf) {
            popup.init(key = node.name, value = node.value)
        } else {
            val value = GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(node.jsonElement)
            popup.init(key = node.name, value = value)
        }

        return popup
    }
}