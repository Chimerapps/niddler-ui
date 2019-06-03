package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.ui.form.NiddlerStructuredViewPopupMenu
import com.icapps.niddler.ui.model.ui.xml.XMLTreeNode
import com.icapps.niddler.ui.model.ui.xml.XMLTreeRenderer
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.awt.Point
import java.awt.event.MouseEvent
import java.io.StringWriter
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


/**
 * @author Koen Van Looveren
 */
class NiddlerXMLTree(documentElement: Element) : JTree() {

    var popup: NiddlerStructuredViewPopupMenu? = null

    init {
        isEditable = false
        showsRootHandles = true
        isRootVisible = true
        model = DefaultTreeModel(XMLTreeNode(documentElement, null), false)

        cellRenderer = XMLTreeRenderer()
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

        val node = path.lastPathComponent as XMLTreeNode
        if (node.isLeaf) {
            popup.init(key = null, value = node.toString())
        } else {
            popup.init(key = null, value = transformToString(node.xmlElement))
        }

        return popup
    }

    private fun transformToString(element: Node): String {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val writer = StringWriter()
        transformer.transform(DOMSource(element), StreamResult(writer))

        return writer.toString()
    }
}