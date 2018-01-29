package com.icapps.niddler.ui.form.debug.nodes.renderer

import com.icapps.niddler.ui.form.debug.nodes.CheckedNode
import java.awt.Component
import java.awt.event.ItemListener
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.AbstractCellEditor
import javax.swing.JTree
import javax.swing.tree.TreeCellEditor


/**
 * @author nicolaverbeeck
 */
class CheckboxCellEditor(private val renderer: CheckedCellRenderer, private val theTree: JTree)
    : AbstractCellEditor(), TreeCellEditor {

    override fun getCellEditorValue(): Any {
        val panel = renderer.panel
        return panel.text.text to panel.checkbox.isSelected
    }

    override fun isCellEditable(event: EventObject): Boolean {
        if (event !is MouseEvent) return false
        val mouseEvent = event

        val path = theTree.getPathForLocation(mouseEvent.x, mouseEvent.y) ?: return false

        val node = path.lastPathComponent as? CheckedNode ?: return false

        return true
    }

    override fun getTreeCellEditorComponent(tree: JTree,
                                            value: Any, selected: Boolean, expanded: Boolean,
                                            leaf: Boolean, row: Int): Component {

        val editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf,
                row, true)

        // editor always selected / focused
        val itemListener = ItemListener {
            if (stopCellEditing()) {
                fireEditingStopped()
            }
        }
        if (editor is CheckedNodePanel) {
            editor.checkbox.addItemListener(itemListener)
        }

        return editor
    }
}