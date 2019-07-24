package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.lib.connection.StaticBlacklistHandler
import com.icapps.niddler.ui.button
import com.icapps.niddler.ui.form.debug.NiddlerStaticBreakpoointsConfigurationDialog
import sun.swing.table.DefaultTableCellHeaderRenderer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Window
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

class SwingNiddlerStaticBlacklistConfigurationDialog(parent: Window?, private val items: List<StaticBlacklistHandler>)
    : JDialog(parent), NiddlerStaticBreakpoointsConfigurationDialog {

    override var visibility: Boolean
        get() = super.isVisible()
        set(value) = super.setVisible(value)

    private var listener: ((changes: List<NiddlerStaticBreakpoointsConfigurationDialog.StaticBlackListChange>) -> Unit)? = null

    init {
        val table = object : JTable() {

            private val headerRenderer = object : DefaultTableCellHeaderRenderer() {
                override fun getTableCellRendererComponent(p0: JTable?, p1: Any?, p2: Boolean, p3: Boolean, p4: Int, p5: Int): Component {
                    return if (p1 is HeaderItem)
                        super.getTableCellRendererComponent(p0, p1.title, p2, p3, p4, p5)
                    else
                        super.getTableCellRendererComponent(p0, p1, p2, p3, p4, p5)
                }
            }

            override fun getCellRenderer(row: Int, column: Int): TableCellRenderer {
                if (super.getValueAt(row, 0) is HeaderItem) {
                    return headerRenderer
                }
                return super.getCellRenderer(row, column)
            }
        }
        val model = object : DefaultTableModel() {

            override fun getColumnClass(columnIndex: Int): Class<*> {
                return when (columnIndex) {
                    1 -> Boolean::class.javaObjectType
                    0 -> String::class.java
                    else -> super.getColumnClass(columnIndex)
                }
            }

            override fun isCellEditable(row: Int, column: Int): Boolean {
                if (super.getValueAt(row, 0) is HeaderItem)
                    return false

                return column == 1
            }
        }

        table.model = model

        model.addColumn("Pattern")
        model.addColumn("Enabled")


        items.forEach {
            val section = HeaderItem(it.name)
            model.addRow(arrayOf(section))
            it.entries.forEach { entry ->
                model.addRow(arrayOf(entry.pattern, entry.enabled))
            }
        }
        val panel = JPanel(BorderLayout())
        panel.add(JScrollPane(table), BorderLayout.CENTER)
        val buttons = JPanel(FlowLayout())
        panel.add(buttons, BorderLayout.SOUTH)

        buttons.add(button("Cancel") { isVisible = false })
        buttons.add(button("Apply") {
            var c = 1
            val changes = items.flatMap { handler ->
                val res = handler.entries.mapNotNull {
                    if (table.getValueAt(c++, 1) != it.enabled)
                        NiddlerStaticBreakpoointsConfigurationDialog.StaticBlackListChange(handler.id, it.pattern, !it.enabled)
                    else
                        null
                }
                ++c
                res
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

    override fun init(applyListener: (changes: List<NiddlerStaticBreakpoointsConfigurationDialog.StaticBlackListChange>) -> Unit) {
        listener = applyListener
    }

}

class HeaderItem(val title: String)