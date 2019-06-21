package com.chimerapps.niddler.ui.model.renderer.impl.form

import com.chimerapps.niddler.ui.component.view.PopupTable
import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.reuseOrNew
import com.chimerapps.niddler.ui.model.renderer.textAreaRenderer
import com.chimerapps.niddler.ui.util.ui.ClipboardUtil
import com.chimerapps.niddler.ui.util.ui.Popup
import com.chimerapps.niddler.ui.util.ui.action
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import javax.swing.JPopupMenu
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

object FormEncodedBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = true
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val data = message.bodyData as? Map<String, List<String>>
        val component = reuseOrNew(reuseComponent) {
            PopupTable(FormEncodedTableModel(emptyMap()), rowAtIndexCb = { model, index -> model.valueAt(index) }, popupMenuForSelectionCb = ::makePopup).also {
                it.fillsViewportHeight = false
                it.rowHeight = 24
                it.showHorizontalLines = true
                it.showVerticalLines = true
                it.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
                it.tableHeader = null

                it.componentPopupMenu = JPopupMenu()
            }
        }
        (component.second.model as FormEncodedTableModel).update(data)
        return component.first
    }

    @Suppress("UNCHECKED_CAST")
    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val data = message.bodyData as? Map<String, List<String>>
        val asString = buildString {
            data?.forEach { key, values ->
                values.forEach { value ->
                    append(key).append(" = ").append(value).append("\n")
                }
            }
        }
        return textAreaRenderer(asString, reuseComponent)
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        return textAreaRenderer(message.getBodyAsString(message.bodyFormat.encoding) ?: "", reuseComponent)
    }

    private fun makePopup(@Suppress("UNUSED_PARAMETER") model: FormEncodedTableModel, data: Pair<String, String>?): JPopupMenu? {
        data ?: return null

        return Popup("Copy key" action { ClipboardUtil.copyToClipboard(StringSelection(data.first)) },
                "Copy value" action { ClipboardUtil.copyToClipboard(StringSelection(data.second)) })
    }
}

private class FormEncodedTableModel(data: Map<String, List<String>>) : AbstractTableModel() {

    private var flatData = data.flatMap { (key, values) -> values.map { key to it } }

    fun update(data: Map<String, List<String>>?) {
        flatData = data?.flatMap { (key, values) -> values.map { key to it } } ?: emptyList()
        fireTableDataChanged()
    }

    override fun getRowCount(): Int = flatData.size

    override fun getColumnCount(): Int = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val entry = flatData[rowIndex]
        return if (columnIndex == 0)
            entry.first
        else
            entry.second
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

    override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java

    fun valueAt(index: Int): Pair<String, String> {
        return flatData[index]
    }

}