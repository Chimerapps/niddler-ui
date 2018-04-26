package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.xml.XMLTreeRenderer
import com.icapps.niddler.ui.model.ui.xml.editor.XMLTreeEditor
import com.icapps.niddler.ui.model.ui.xml.editor.XMLTreeEditorNode
import org.xml.sax.InputSource
import java.awt.event.KeyEvent
import java.io.StringReader
import javax.swing.DropMode
import javax.swing.JTree
import javax.swing.KeyStroke
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Koen Van Looveren
 */
class NiddlerXMLEditableTree(message: ParsedNiddlerMessage) : JTree() {

    init {
        isEditable = true
        dragEnabled = true
        dropMode = DropMode.ON_OR_INSERT
        setShowsRootHandles(true)
        isRootVisible = true

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val ins = InputSource(StringReader("<person gender=\"female\"><firstname>Anna</firstname><lastname>Smith</lastname></person>"))
        val document = builder.parse(ins)

        model = DefaultTreeModel(XMLTreeEditorNode(document, null, null), false)

        setCellEditor(XMLTreeEditor(this))
        setCellRenderer(XMLTreeRenderer())
        //transferHandler = JsonTreeEditorTransferHandler()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing")
    }

    /*
    fun getEditedXML(): Document {
        return (model.root as XMLTreeEditorNode).
    }
    */
}