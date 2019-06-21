package com.chimerapps.niddler.ui.model.renderer.impl.xml

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.reuseOrNew
import com.chimerapps.niddler.ui.model.renderer.textAreaRenderer
import com.chimerapps.niddler.ui.util.ui.loadIcon
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBFont
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.Font
import java.awt.Point
import java.awt.event.MouseEvent
import java.io.StringWriter
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object XMLBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = true
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val data = (message.bodyData as? Document)
        val component = reuseOrNew(reuseComponent) { NiddlerXmlTree() }
        component.second.resetModel(data?.documentElement)
        return component.first
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, message.bodyFormat.encoding ?: "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val writer = StringWriter()
        transformer.transform(DOMSource(message.bodyData as Document?), StreamResult(writer))

        return textAreaRenderer(writer.toString(), reuseComponent)
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val stringData = message.message.getBodyAsString(message.bodyFormat.encoding) ?: ""
        return textAreaRenderer(stringData, reuseComponent)
    }

}

private class NiddlerXmlTree : Tree() {

    //var popup: NiddlerStructuredViewPopupMenu? = null

    init {
        isEditable = false
        setShowsRootHandles(true)
        isRootVisible = true

        setCellRenderer(XmlTreeCellRenderer())
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        transferHandler = XmlTreeTransferHandler(transferHandler)
    }

    fun resetModel(root: Element?) {
        model = if (root == null)
            DefaultTreeModel(DefaultMutableTreeNode())
        else
            DefaultTreeModel(XMLTreeNode(root, null), false)
    }

    override fun getPopupLocation(event: MouseEvent): Point? {
        val path = getClosestPathForLocation(event.x, event.y)
        clearSelection()
        selectionModel.selectionPath = path
        return super.getPopupLocation(event)
    }

//    override fun getComponentPopupMenu(): JPopupMenu? {
//        val path = selectionPath ?: return null
//        val popup = popup ?: return null
//
//        val node = path.lastPathComponent as JsonTreeNode
//        if (node.isLeaf) {
//            popup.init(key = node.name, value = node.value)
//        } else {
//            val value = GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(node.jsonElement)
//            popup.init(key = node.name, value = value)
//        }
//
//        return popup
//    }
}


private class XmlTreeCellRenderer : ColoredTreeCellRenderer() {

    private val stringIcon = loadIcon("/ic_string.png")
    private val nodeIcon = AllIcons.Nodes.Folder
    private val commentIcon = AllIcons.Nodes.Advice
    private val monoSpaced = JBFont.create(Font("Monospaced", 0, 12))

    override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        font = monoSpaced
        if (value is XMLTreeNode) {
            var italic = false
            icon = when (value.type) {
                XMLTreeNode.Type.NODE -> nodeIcon
                XMLTreeNode.Type.TEXT -> stringIcon
                XMLTreeNode.Type.COMMENT -> {
                    italic = true
                    commentIcon
                }
            }
            if (italic)
                append(value.toString(), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES)
            else
                append(value.toString())
        }
    }

}