package com.icapps.niddler.ui.form.detail.body

import com.google.gson.JsonElement
import com.icapps.niddler.ui.model.ui.json.JsonTreeNode
import com.icapps.niddler.ui.model.ui.json.JsonTreeRenderer
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Koen Van Looveren
 */
class NiddlerJsonTree(json: JsonElement) : JTree() {

    init {
        isEditable = false
        setShowsRootHandles(true)
        isRootVisible = true
        model = DefaultTreeModel(JsonTreeNode(json, null, null), false)

        setCellRenderer(JsonTreeRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }
}