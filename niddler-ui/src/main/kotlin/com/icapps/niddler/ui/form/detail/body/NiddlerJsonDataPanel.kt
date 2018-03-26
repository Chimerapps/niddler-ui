package com.icapps.niddler.ui.form.detail.body

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.json.JsonTreeNode
import com.icapps.niddler.ui.model.ui.json.JsonTreeRenderer
import javax.swing.JTree
import javax.swing.text.Document

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerJsonDataPanel(message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(true, true, message) {

    init {
        initUI()
    }

    override fun createStructuredView() {
/*
        //todo remove from comments
        val structuredView = JTree()
        structuredView.isEditable = false
        structuredView.showsRootHandles = true
        structuredView.isRootVisible = true
        structuredView.model = DefaultTreeModel(JsonTreeNode(message.bodyData as JsonElement, null, null), false)

        structuredView.cellRenderer = JsonTreeRenderer()
        structuredView.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
*/
        this.structuredView = NiddlerEditableJsonTree(message)
    }

    override fun createPrettyPrintedView(doc: Document) {
        doc.remove(0, doc.length)
        doc.insertString(0, GsonBuilder().setPrettyPrinting().create().toJson(message.bodyData), null)
    }

}