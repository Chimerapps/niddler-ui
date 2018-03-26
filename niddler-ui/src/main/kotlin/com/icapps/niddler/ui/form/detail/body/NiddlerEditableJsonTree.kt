package com.icapps.niddler.ui.form.detail.body

import com.google.gson.JsonElement
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.json.EditableJsonTreeNode
import com.icapps.niddler.ui.model.ui.json.JsonTreeEditor
import com.icapps.niddler.ui.model.ui.json.JsonTreeEditorTransferHandler
import com.icapps.niddler.ui.model.ui.json.JsonTreeRenderer
import java.awt.event.KeyEvent
import javax.swing.DropMode
import javax.swing.JTree
import javax.swing.KeyStroke
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Koen Van Looveren
 */
class NiddlerEditableJsonTree(message: ParsedNiddlerMessage) : JTree() {

    init {
        isEditable = true
        dragEnabled = true
        dropMode = DropMode.ON_OR_INSERT
        setShowsRootHandles(true)
        isRootVisible = true
        model = DefaultTreeModel(EditableJsonTreeNode(message.bodyData as JsonElement, null, null ), false)

        setCellEditor(JsonTreeEditor(this))
        setCellRenderer(JsonTreeRenderer())
        transferHandler = JsonTreeEditorTransferHandler()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing")
    }
}