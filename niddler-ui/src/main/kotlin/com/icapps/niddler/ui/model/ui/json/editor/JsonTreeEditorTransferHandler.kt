package com.icapps.niddler.ui.model.ui.json.editor

import com.icapps.niddler.ui.model.ui.json.JsonNode
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultTreeModel

/**
 * @author Koen Van Looveren
 */
class JsonTreeEditorTransferHandler : TransferHandler() {

    lateinit var nodesFlavor: DataFlavor
    lateinit var nodesToRemove: Array<JsonTreeEditorNode>
    var flavors = arrayOfNulls<DataFlavor>(1)

    init {
        try {
            val mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Array<JsonTreeEditorNode>::class.java.name + "\""
            nodesFlavor = DataFlavor(mimeType)
            flavors[0] = nodesFlavor
        } catch (e: ClassNotFoundException) {
            println("ClassNotFound: " + e.message)
        }
    }

    override fun canImport(support: TransferHandler.TransferSupport): Boolean {
        if (!support.isDrop) {
            return false
        }
        support.setShowDropLocation(true)
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            return false
        }
        val action = support.dropAction
        if (action == TransferHandler.MOVE) {
            val dl = support.dropLocation as JTree.DropLocation
            val dest = dl.path
            val target = dest.lastPathComponent as JsonTreeEditorNode
            val targetParent = dest.lastPathComponent
            if (targetParent is JsonTreeEditorNode)
                if (target.actualType() == JsonNode.JsonDataType.ARRAY) {
                    return true
                }
        }
        return false
    }

    override fun createTransferable(c: JComponent): Transferable? {
        val tree = c as JTree
        val target = tree.lastSelectedPathComponent as JsonTreeEditorNode
        val parent = target.parent as? JsonTreeEditorNode ?: return null
        if (parent.actualType() != JsonNode.JsonDataType.ARRAY) {
            return null
        }
        val paths = tree.selectionPaths
        if (paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            val copies = ArrayList<JsonTreeEditorNode>()
            val toRemove = ArrayList<JsonTreeEditorNode>()
            val node = paths[0].lastPathComponent as JsonTreeEditorNode
            val copy = copy(node)
            copies.add(copy)
            toRemove.add(node)
            for (i in 1 until paths.size) {
                val next = paths[i].lastPathComponent as JsonTreeEditorNode
                copies.add(copy(next))
                toRemove.add(next)
            }
            val nodes = copies.toTypedArray()
            nodesToRemove = toRemove.toTypedArray()
            return NodesTransferable(nodes)
        }
        return null
    }

    /** Defensive copy used in createTransferable.  */
    private fun copy(node: JsonTreeEditorNode): JsonTreeEditorNode {
        return JsonTreeEditorNode(node.jsonElement, node.parent as JsonTreeEditorNode, node.name)
    }

    override fun exportDone(source: JComponent?, data: Transferable?, action: Int) {
        if (action and TransferHandler.MOVE == TransferHandler.MOVE) {
            val tree = source as JTree?
            val model = tree!!.model as DefaultTreeModel
            nodesToRemove.forEach { model.removeNodeFromParent(it) }
        }
    }

    override fun getSourceActions(c: JComponent): Int {
        return TransferHandler.MOVE
    }

    override fun importData(support: TransferHandler.TransferSupport): Boolean {
        if (!canImport(support)) {
            return false
        }
        // Extract transfer data.
        var nodes: Array<JsonTreeEditorNode>? = null
        try {
            val t = support.transferable
            nodes = t.getTransferData(nodesFlavor) as Array<JsonTreeEditorNode>
        } catch (ufe: UnsupportedFlavorException) {
            println("UnsupportedFlavor: " + ufe.message)
        } catch (ioe: java.io.IOException) {
            println("I/O error: " + ioe.message)
        }

        // Get drop location info.
        val dl = support.dropLocation as JTree.DropLocation
        val childIndex = dl.childIndex
        val dest = dl.path
        val parent = dest.lastPathComponent as JsonTreeEditorNode
        val tree = support.component as JTree
        val model = tree.model as DefaultTreeModel
        // Configure for drop mode.
        var index = childIndex    // DropMode.INSERT
        if (childIndex == -1) {     // DropMode.ON
            index = parent.childCount
        }
        // Add data to model.
        nodes?.forEach {
            model.insertNodeInto(it, parent, index++)
        }
        return true
    }

    override fun toString(): String {
        return javaClass.name
    }

    inner class NodesTransferable(internal var nodes: Array<JsonTreeEditorNode>) : Transferable {

        @Throws(UnsupportedFlavorException::class)
        override fun getTransferData(flavor: DataFlavor): Any {
            if (!isDataFlavorSupported(flavor))
                throw UnsupportedFlavorException(flavor)
            return nodes
        }

        override fun getTransferDataFlavors(): Array<DataFlavor?> {
            return flavors
        }

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
            return nodesFlavor.equals(flavor)
        }
    }
}