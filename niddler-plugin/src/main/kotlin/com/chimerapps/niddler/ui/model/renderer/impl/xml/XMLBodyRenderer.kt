package com.chimerapps.niddler.ui.model.renderer.impl.xml

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.reuseOrNew
import com.chimerapps.niddler.ui.model.renderer.textAreaRenderer
import com.chimerapps.niddler.ui.util.localization.Tr
import com.chimerapps.niddler.ui.util.ui.ClipboardUtil
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.chimerapps.niddler.ui.util.ui.Popup
import com.chimerapps.niddler.ui.util.ui.PopupAction
import com.chimerapps.niddler.ui.util.ui.action
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBFont
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.Font
import java.awt.Point
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

object XMLBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = true
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        val data = (message.bodyData as? Document)
        val component = reuseOrNew(project, "xmlTree", reuseComponent) {
            NiddlerXmlTree().also {
                TreeSpeedSearch(it, { path -> path.lastPathComponent.toString() }, true)
            }
        }
        component.second.resetModel(data?.documentElement)
        return component.first
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        return textAreaRenderer(prettyText(message.bodyData), reuseComponent, project, XmlFileType.INSTANCE, requestFocus)
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        val stringData = message.message.getBodyAsString(message.bodyFormat.encoding) ?: ""
        return textAreaRenderer(stringData, reuseComponent, project, XmlFileType.INSTANCE, requestFocus)
    }

    override fun prettyText(bodyData: Any?): String {
        val data = (bodyData as? Document)
        return data?.let { XmlTreeTransferHandler.formatXML(it) } ?: ""
    }
}

private class NiddlerXmlTree : Tree() {

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

    override fun getComponentPopupMenu(): JPopupMenu? {
        val path = selectionPath ?: return null

        val node = path.lastPathComponent as XMLTreeNode
        val actions = mutableListOf<PopupAction>()
        actions += Tr.ViewXmlActionCopyTree.tr() action {
            ClipboardUtil.copyToClipboard(StringSelection(XmlTreeTransferHandler.formatXML(node.xmlElement)))
        }
        actions += Tr.ViewXmlActionCopyValue.tr() action { ClipboardUtil.copyToClipboard(StringSelection(node.toString())) }

        return Popup(actions)
    }
}


private class XmlTreeCellRenderer : ColoredTreeCellRenderer() {

    private val stringIcon = IncludedIcons.Types.string
    private val nodeIcon = AllIcons.Nodes.Folder
    private val commentIcon = AllIcons.Nodes.Tag
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