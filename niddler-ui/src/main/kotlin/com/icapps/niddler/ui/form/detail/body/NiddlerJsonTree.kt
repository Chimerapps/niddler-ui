package com.icapps.niddler.ui.form.detail.body

import com.google.gson.JsonElement
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.json.JsonTreeNode
import com.icapps.niddler.ui.model.ui.json.JsonTreeRenderer
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Koen Van Looveren
 */
class NiddlerJsonTree(message: ParsedNiddlerMessage) : JTree() {

    init {
        isEditable = false
        showsRootHandles = true
        isRootVisible = true
        model = DefaultTreeModel(JsonTreeNode(message.bodyData as JsonElement, null, null), false)

        cellRenderer = JsonTreeRenderer()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }
}