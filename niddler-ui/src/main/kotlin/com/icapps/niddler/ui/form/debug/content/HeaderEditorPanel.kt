package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.form.ui.makeAction
import com.icapps.niddler.ui.util.loadIcon
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JToolBar
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

/**
 * @author nicolaverbeeck
 */
class HeaderEditorPanel(private val changeListener: () -> Unit) : JPanel(BorderLayout()) {

    private companion object {
        private val alternateRowColor1 = Color(0xD6, 0xDF, 0xEB)
        private val alternateRowColor2 = Color(0xE0, 0xE0, 0xE0)
    }

    private val table = object : JTable() {
        override fun prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int): Component {
            val component = super.prepareRenderer(renderer, row, column)
            if (component.background != getSelectionBackground()) {
                val bg: Color? = if (row % 2 == 1) alternateRowColor1 else alternateRowColor2
                component.background = bg
            }
            return component
        }
    }

    private val model = DefaultTableModel()

    init {
        val tableScroller = JScrollPane(table)
        tableScroller.border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2), tableScroller.border)
        add(tableScroller, BorderLayout.CENTER)
        table.rowHeight = 28
        table.font = table.font.deriveFont(15.0f)

        val toolbar = JToolBar()
        toolbar.isFloatable = false

        toolbar.add(makeAction("Add header", loadIcon("/add.png")) {
            model.addRow(arrayOf("", ""))
        })
        toolbar.add(makeAction("Add header", loadIcon("/remove.png")) {
            if (model.rowCount == 0)
                return@makeAction
            if (table.selectedRow != -1)
                model.removeRow(table.selectedRow)
            else
                model.removeRow(table.rowCount - 1)
        })

        val toolbarFrame = JPanel(BorderLayout())
        toolbarFrame.add(toolbar, BorderLayout.EAST)
        add(toolbarFrame, BorderLayout.SOUTH)

        model.addColumn("Name")
        model.addColumn("Value")
        table.model = model
        table.putClientProperty("terminateEditOnFocusLost", true)

        model.addTableModelListener { changeListener() }

        maximumSize = Dimension(maximumSize.width, 100)
    }

    fun extractHeaders(): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()

        val count = model.rowCount
        for (i in 0 until count) {
            val key = model.getValueAt(i, 0).toString().trim()
            val value = model.getValueAt(i, 1).toString().trim()

            if (key.isEmpty() || value.isEmpty())
                continue
            map.getOrPut(key) { mutableListOf() } += value
        }

        return map
    }

    fun init(headers: Map<String, List<String>>?) {
        val count = model.rowCount
        for (i in 0 until count)
            model.removeRow(0)
        headers?.forEach { key, value ->
            value.forEach {
                model.addRow(arrayOf(key, it))
            }
        }
    }

}