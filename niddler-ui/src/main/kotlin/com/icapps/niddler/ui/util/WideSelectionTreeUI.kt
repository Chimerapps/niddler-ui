package com.icapps.niddler.ui.util

import java.awt.*
import javax.swing.*
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.TreePath

/**
 * @author Nicola Verbeeck
 * @date 02/05/2017.
 */
class WideSelectionTreeUI : BasicTreeUI() {

    companion object {
        private val LIST_BACKGROUND_PAINTER = UIManager.getBorder("List.sourceListBackgroundPainter")
        private val LIST_SELECTION_BACKGROUND_PAINTER = UIManager.getBorder("List.sourceListSelectionBackgroundPainter")
        private val LIST_FOCUSED_SELECTION_BACKGROUND_PAINTER = UIManager.getBorder("List.sourceListFocusedSelectionBackgroundPainter")
    }


    override fun paintRow(g: Graphics, clipBounds: Rectangle?, insets: Insets?, bounds: Rectangle, path: TreePath?, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        val containerWidth = if (this.tree.parent is JViewport) this.tree.parent.width else this.tree.width
        val xOffset = if (this.tree.parent is JViewport) (this.tree.parent as JViewport).viewPosition.x else 0
        if (path != null) {
            val selected = this.tree.isPathSelected(path)
            val rowGraphics = g.create() as Graphics2D
            rowGraphics.clip = clipBounds
            val sourceList = this.tree.getClientProperty("mac.ui.source.list")
            var background = this.tree.background
            if (row % 2 == 0 && java.lang.Boolean.TRUE == this.tree.getClientProperty("mac.ui.striped")) {
                background = UIUtil.getDecoratedRowColor()
            }

            if (sourceList != null && sourceList as Boolean) {
                if (selected) {
                    if (this.tree.hasFocus()) {
                        LIST_FOCUSED_SELECTION_BACKGROUND_PAINTER.paintBorder(this.tree, rowGraphics, xOffset, bounds.y, containerWidth, bounds.height)
                    } else {
                        LIST_SELECTION_BACKGROUND_PAINTER.paintBorder(this.tree, rowGraphics, xOffset, bounds.y, containerWidth, bounds.height)
                    }
                } else {
                    rowGraphics.color = background
                    rowGraphics.fillRect(xOffset, bounds.y, containerWidth, bounds.height)
                }
            } else if (selected && (UIUtil.isUnderAquaBasedLookAndFeel() || UIUtil.isUnderDarcula() || UIUtil.isUnderIntelliJLaF())) {
                val bg = getSelectionBackground(this.tree, true)
                rowGraphics.color = bg
                rowGraphics.fillRect(xOffset, bounds.y, containerWidth, bounds.height)
            }

            if (this.shouldPaintExpandControl(path, row, isExpanded, hasBeenExpanded, isLeaf)) {
                this.paintExpandControl(rowGraphics, bounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf)
            }

            super.paintRow(rowGraphics, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf)
            rowGraphics.dispose()
        } else {
            super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf)
        }

    }

    override fun paint(g: Graphics, c: JComponent) {
        if (!UIUtil.isUnderAquaBasedLookAndFeel() && !UIUtil.isUnderDarcula() && !UIUtil.isUnderIntelliJLaF()) {
            this.paintSelectedRows(g, c as JTree)
        }

        val containerWidth = if (this.tree.parent is JViewport) this.tree.parent.width else this.tree.width
        val xOffset = if (this.tree.parent is JViewport) (this.tree.parent as JViewport).viewPosition.x else 0
        val bounds = g.clipBounds
        val sourceList = this.tree.getClientProperty("mac.ui.source.list")
        if (sourceList != null && (sourceList as Boolean)) {
            val backgroundGraphics = g.create() as Graphics2D
            backgroundGraphics.setClip(xOffset, bounds.y, containerWidth, bounds.height)
            LIST_BACKGROUND_PAINTER.paintBorder(this.tree, backgroundGraphics, xOffset, bounds.y, containerWidth, bounds.height)
            backgroundGraphics.dispose()
        }


        super.paint(g, c)
    }

    private fun paintSelectedRows(g: Graphics, tr: JTree) {
        val rect = tr.visibleRect
        val firstVisibleRow = tr.getClosestRowForLocation(rect.x, rect.y)
        val lastVisibleRow = tr.getClosestRowForLocation(rect.x, rect.y + rect.height)

        for (row in firstVisibleRow..lastVisibleRow) {
            if (tr.selectionModel.isRowSelected(row)) {
                val bounds = tr.getRowBounds(row)
                val color = getSelectionBackground(tr, false)
                if (color != null) {
                    g.color = color
                    g.fillRect(0, bounds.y, tr.width, bounds.height)
                }
            }
        }

    }

    override fun createCellRendererPane(): CellRendererPane {
        return object : CellRendererPane() {
            override fun paintComponent(g: Graphics, c: Component?, p: Container?, x: Int, y: Int, w: Int, h: Int, shouldValidate: Boolean) {
                if (c is JComponent && c.isOpaque) {
                    c.isOpaque = false
                }

                super.paintComponent(g, c, p, x, y, w, h, shouldValidate)
            }
        }
    }

    override fun paintExpandControl(g: Graphics, clipBounds: Rectangle?, insets: Insets?, bounds: Rectangle, path: TreePath, row: Int, isExpanded: Boolean, hasBeenExpanded: Boolean, isLeaf: Boolean) {
        val isPathSelected = this.tree.selectionModel.isPathSelected(path)
        if (!this.isLeaf(row)) {
            this.setExpandedIcon(UIUtil.getTreeNodeIcon(true, isPathSelected, this.tree.hasFocus()))
            this.setCollapsedIcon(UIUtil.getTreeNodeIcon(false, isPathSelected, this.tree.hasFocus()))
        }

        super.paintExpandControl(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf)
    }

    private fun getSelectionBackground(tree: JTree, checkProperty: Boolean): Color? {
        val property = tree.getClientProperty("TreeTableTree")
        if (property is JTable) {
            return property.selectionBackground
        } else {
            var selection = tree.hasFocus()
            if (!selection && checkProperty) {
                selection = java.lang.Boolean.TRUE == property
            }

            return UIUtil.getTreeSelectionBackground(selection)
        }
    }
}