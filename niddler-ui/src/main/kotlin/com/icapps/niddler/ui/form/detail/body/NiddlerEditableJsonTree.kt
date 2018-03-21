package com.icapps.niddler.ui.form.detail.body

import com.google.gson.JsonElement
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.json.EditableJsonTreeNode
import com.icapps.niddler.ui.model.ui.json.JsonTreeEditor
import com.icapps.niddler.ui.model.ui.json.JsonTreeRenderer
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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
        setShowsRootHandles(true)
        isRootVisible = true
        model = DefaultTreeModel(EditableJsonTreeNode(message.bodyData as JsonElement, null, null), false)

        setCellEditor(JsonTreeEditor(this))
        setCellRenderer(JsonTreeRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing")
        addListeners()
    }

    private fun addListeners() {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val row = getRowForLocation(e.x, e.y)
                val path = getPathForLocation(e.x, e.y)
                if (row != -1) {
                    if (e.clickCount == AMOUNT_OF_EDIT_CLICKS) {
                        startEditingAtPath(path)
                    }
                }
            }
        })
    }

    companion object {
        private const val AMOUNT_OF_EDIT_CLICKS = 2
    }
}