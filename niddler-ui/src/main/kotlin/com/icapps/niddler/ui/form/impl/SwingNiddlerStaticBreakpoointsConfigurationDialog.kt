package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.button
import com.icapps.niddler.ui.form.debug.NiddlerStaticBreakpoointsConfigurationDialog
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Window
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class SwingNiddlerStaticBreakpoointsConfigurationDialog(parent: Window?, private val items: List<Pair<String, Boolean>>)
    : JDialog(parent), NiddlerStaticBreakpoointsConfigurationDialog {

    override var visibility: Boolean
        get() = super.isVisible()
        set(value) = super.setVisible(value)

    private var listener: ((changes: List<Pair<String, Boolean>>) -> Unit)? = null

    init {
        val table = JTable()
        val model = object : DefaultTableModel() {
            override fun getColumnClass(columnIndex: Int): Class<*> {
                return when (columnIndex) {
                    1 -> Boolean::class.javaObjectType
                    else -> super.getColumnClass(columnIndex)
                }
            }

            override fun isCellEditable(row: Int, column: Int): Boolean {
                return column == 1
            }
        }
        table.model = model

        model.addColumn("Pattern")
        model.addColumn("Enabled")

        items.forEach {
            model.addRow(arrayOf(it.first, it.second))
        }
        val panel = JPanel(BorderLayout())
        panel.add(JScrollPane(table), BorderLayout.CENTER)
        val buttons = JPanel(FlowLayout())
        panel.add(buttons, BorderLayout.SOUTH)

        buttons.add(button("Cancel") { isVisible = false })
        buttons.add(button("Apply") {
            val changes = items.mapIndexedNotNull { index, pair ->
                if (table.getValueAt(index, 1) != pair.second) {
                    pair.first to !pair.second
                } else {
                    null
                }
            }
            if (changes.isNotEmpty()) {
                listener?.invoke(changes)
            }
            isVisible = false
        })

        contentPane.add(panel)

        setSize(400, 400)
        if (parent != null)
            setLocationRelativeTo(parent)
    }

    override fun init(applyListener: (changes: List<Pair<String, Boolean>>) -> Unit) {
        listener = applyListener
    }

}