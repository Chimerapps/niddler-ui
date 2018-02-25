package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.util.simpleAction
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
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
        add(JScrollPane(table), BorderLayout.CENTER)
        val toolbar = JToolBar()
        toolbar.isFloatable = false
        toolbar.add(simpleAction(icon = "/add.png") {
            model.addRow(arrayOf("", ""))
        })
        toolbar.add(simpleAction(icon = "/remove.png") {
            if (model.rowCount == 0)
                return@simpleAction
            if (table.selectedRow != -1)
                model.removeRow(table.selectedRow)
            else
                model.removeRow(table.rowCount - 1)
        })
        add(toolbar, BorderLayout.SOUTH)

        model.addColumn("Name")
        model.addColumn("Value")
        table.model = model
        table.putClientProperty("terminateEditOnFocusLost", true)

        model.addTableModelListener { changeListener() }

        maximumSize = Dimension(maximumSize.width, 100)
    }

    fun extractHeaders(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        val count = model.rowCount
        for (i in 0 until count) {
            val key = model.getValueAt(i, 0).toString().trim()
            val value = model.getValueAt(i, 1).toString().trim()

            if (key.isEmpty() || value.isEmpty())
                continue
            map[key] = value
        }

        return map
    }

    fun init(headers: Map<String, String>?) {
        val count = model.rowCount
        for (i in 0 until count)
            model.removeRow(0)
        headers?.forEach { key, value ->
            model.addRow(arrayOf(key, value))
        }
    }

}