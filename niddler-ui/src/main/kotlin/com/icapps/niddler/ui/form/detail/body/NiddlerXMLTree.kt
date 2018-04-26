package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.xml.XMLTreeRenderer
import com.icapps.niddler.ui.model.ui.xml.XMLTreeNode
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Koen Van Looveren
 */
class NiddlerXMLTree(message: ParsedNiddlerMessage) : JTree() {

    init {
        isEditable = false
        showsRootHandles = true
        isRootVisible = true

        model = DefaultTreeModel(XMLTreeNode((message.bodyData as org.w3c.dom.Document).documentElement, null, null), false)

        setCellRenderer(XMLTreeRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }
}