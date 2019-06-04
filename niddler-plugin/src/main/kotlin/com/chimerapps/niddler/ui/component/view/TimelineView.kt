package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ui.loadIcon
import com.chimerapps.niddler.ui.util.ui.setColumnPreferredWidth
import com.icapps.niddler.lib.connection.model.NetworkNiddlerMessage
import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.utils.getStatusCodeString
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

class TimelineView(messageContainer: NiddlerMessageStorage<ParsedNiddlerMessage>) : JPanel(BorderLayout()), MessagesView {

    private val model = LinkedTableModel(messageContainer)
    private val tableView = JBTable(model).apply {
        fillsViewportHeight = false
        rowHeight = 32
        showHorizontalLines = true
        showVerticalLines = true
        autoResizeMode = JTable.AUTO_RESIZE_OFF
        tableHeader = null

        addMouseListener(TableResizeAdapter(this))

        setColumnPreferredWidth(LinkedTableModel.INDEX_TIMESTAMP, 90)
        setColumnPreferredWidth(LinkedTableModel.INDEX_DIRECTION, 36)
        setColumnPreferredWidth(LinkedTableModel.INDEX_METHOD, 70)
        setColumnPreferredWidth(LinkedTableModel.INDEX_URL, 400)
    }

    init {
        add(JBScrollPane(tableView), BorderLayout.CENTER)
    }

    override var urlHider: BaseUrlHider?
        get() = model.urlHider
        set(value) {
            model.urlHider = value
        }

    override fun onMessagesUpdated() = model.onMessagesUpdated()

}

class LinkedTableModel(private val messageContainer: NiddlerMessageStorage<ParsedNiddlerMessage>) : AbstractTableModel(), MessagesView {

    companion object {
        const val INDEX_TIMESTAMP = 0
        const val INDEX_DIRECTION = 1
        const val INDEX_METHOD = 2
        const val INDEX_URL = 3
        const val INDEX_STATUS_CODE = 4
        const val INDEX_FORMAT = 5
    }

    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    override var urlHider: BaseUrlHider? = null
        set(value) {
            field = value
            fireTableDataChanged()
        }

    private val upIcon = loadIcon("/ic_up.png")
    private val downIcon = loadIcon("/ic_down.png")
    private var messages: List<ParsedNiddlerMessage> = listOf(ParsedNiddlerMessage(
            NetworkNiddlerMessage(
                    "1",
                    "2",
                    System.currentTimeMillis(),
                    "https://www.google.com",
                    "GET",
                    null,
                    null,
                    200,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ),
            BodyFormat(BodyFormatType.FORMAT_XML, null, null),
            null,
            null,
            null
    ))

    init {
    }

    override fun onMessagesUpdated() {
        //TODO optimized
        messages = messageContainer.messagesChronological
        fireTableDataChanged()
    }

    override fun getRowCount(): Int = messages.size

    override fun getColumnCount(): Int = 6

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val message = messages[rowIndex]
        val other = if (message.isRequest) messageContainer.findResponse(message) else messageContainer.findRequest(message)

        return when (columnIndex) {
            INDEX_TIMESTAMP -> timeFormatter.format(Date(message.timestamp))
            INDEX_DIRECTION -> if (message.isRequest) upIcon else downIcon
            INDEX_METHOD -> message.method ?: other?.method
            INDEX_URL -> hideBaseUrl(message.url ?: other?.url)
            INDEX_STATUS_CODE -> {
                if (!message.message.statusLine.isNullOrBlank())
                    "${message.statusCode} - ${message.message.statusLine}"
                else if (!other?.message?.statusLine.isNullOrBlank())
                    "${other?.message?.statusCode} - ${other?.message?.statusLine}"
                else if (message.statusCode != null)
                    formatStatusCode(message.statusCode)
                else
                    formatStatusCode(other?.statusCode)
            }
            INDEX_FORMAT -> message.bodyFormat
            else -> "<NO COLUMN DEF>"
        }
    }

    override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
        INDEX_TIMESTAMP -> "Timestamp"
        INDEX_DIRECTION -> "Up/Down"
        INDEX_METHOD -> "Method"
        INDEX_URL -> "Url"
        INDEX_STATUS_CODE -> "Status"
        INDEX_FORMAT -> "Format"
        else -> "<NO COLUMN NAME>"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> = when (columnIndex) {
        INDEX_TIMESTAMP, INDEX_URL, INDEX_METHOD -> String::class.java
        INDEX_DIRECTION -> Icon::class.java
        INDEX_STATUS_CODE -> String::class.java
        INDEX_FORMAT -> String::class.java
        else -> String::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

    private fun formatStatusCode(statusCode: Int?): String {
        return if (statusCode == null) {
            ""
        } else {
            String.format("%d - %s", statusCode, getStatusCodeString(statusCode))
        }
    }

    private fun hideBaseUrl(url: String?): String? {
        url ?: return null
        val hider = urlHider ?: return url
        val baseToStrip = hider.getHiddenBaseUrl(url) ?: return url

        //u2026 = …
        return "\u2026" + url.substring(baseToStrip.length)
    }
}