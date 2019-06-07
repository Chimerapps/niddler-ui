package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.model.AppPreferences
import com.chimerapps.niddler.ui.util.ui.dispatchMain
import com.chimerapps.niddler.ui.util.ui.loadIcon
import com.chimerapps.niddler.ui.util.ui.setColumnPreferredWidth
import com.icapps.niddler.lib.connection.model.NetworkNiddlerMessage
import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.ChronologicalMessagesView
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.utils.getStatusCodeString
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel

class TimelineView(messageContainer: NiddlerMessageStorage<ParsedNiddlerMessage>,
                   private val selectionListener: MessageSelectionListener) : JPanel(BorderLayout()), MessagesView {

    private companion object {
        private const val PREFERENCE_KEY_TIMESTAMP_WIDTH = "timeline.timestamp.width"
        private const val PREFERENCE_KEY_INDEX_DIRECTION_WIDTH = "timeline.direction.width"
        private const val PREFERENCE_KEY_INDEX_METHOD_WIDTH = "timeline.method.width"
        private const val PREFERENCE_KEY_INDEX_URL_WIDTH = "timeline.url.width"
        private const val PREFERENCE_KEY_INDEX_STATUS_WIDTH = "timeline.status.width"
        private const val PREFERENCE_KEY_INDEX_FORMAT_WIDTH = "timeline.format.width"

        private const val DEFAULT_TIMESTAMP_WIDTH = 90
        private const val DEFAULT_DIRECTION_WIDTH = 36
        private const val DEFAULT_METHOD_WIDTH = 70
        private const val DEFAULT_URL_WIDTH = 400
        private const val DEFAULT_STATUS_WIDTH = -1
        private const val DEFAULT_FORMAT_WIDTH = -1
    }

    private val model = TimelineTableModel(messageContainer)
    private val tableView = JBTable(model).apply {
        fillsViewportHeight = false
        rowHeight = 32
        showHorizontalLines = true
        showVerticalLines = true
        autoResizeMode = JTable.AUTO_RESIZE_OFF
        tableHeader = null

        selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        selectionModel.addListSelectionListener {
            val row = selectedRow
            selectionListener.onMessageSelectionChanged(this@TimelineView.model.getMessageAtRow(row))
        }

        addMouseListener(TableResizeAdapter(this) { columnIndex, newWidth ->
            val key = when (columnIndex) {
                TimelineTableModel.INDEX_TIMESTAMP -> PREFERENCE_KEY_TIMESTAMP_WIDTH
                TimelineTableModel.INDEX_DIRECTION -> PREFERENCE_KEY_INDEX_DIRECTION_WIDTH
                TimelineTableModel.INDEX_METHOD -> PREFERENCE_KEY_INDEX_METHOD_WIDTH
                TimelineTableModel.INDEX_URL -> PREFERENCE_KEY_INDEX_URL_WIDTH
                TimelineTableModel.INDEX_STATUS_CODE -> PREFERENCE_KEY_INDEX_STATUS_WIDTH
                TimelineTableModel.INDEX_FORMAT -> PREFERENCE_KEY_INDEX_FORMAT_WIDTH
                else -> return@TableResizeAdapter
            }
            val defaultForKey = when (columnIndex) {
                TimelineTableModel.INDEX_TIMESTAMP -> DEFAULT_TIMESTAMP_WIDTH
                TimelineTableModel.INDEX_DIRECTION -> DEFAULT_DIRECTION_WIDTH
                TimelineTableModel.INDEX_METHOD -> DEFAULT_METHOD_WIDTH
                TimelineTableModel.INDEX_URL -> DEFAULT_URL_WIDTH
                TimelineTableModel.INDEX_STATUS_CODE -> DEFAULT_STATUS_WIDTH
                TimelineTableModel.INDEX_FORMAT -> DEFAULT_FORMAT_WIDTH
                else -> return@TableResizeAdapter
            }
            AppPreferences.put(key, newWidth, defaultForKey)
        })

        setColumnPreferredWidth(TimelineTableModel.INDEX_TIMESTAMP, AppPreferences.get(PREFERENCE_KEY_TIMESTAMP_WIDTH, DEFAULT_TIMESTAMP_WIDTH))
        setColumnPreferredWidth(TimelineTableModel.INDEX_DIRECTION, AppPreferences.get(PREFERENCE_KEY_INDEX_DIRECTION_WIDTH, DEFAULT_DIRECTION_WIDTH))
        setColumnPreferredWidth(TimelineTableModel.INDEX_METHOD, AppPreferences.get(PREFERENCE_KEY_INDEX_METHOD_WIDTH, DEFAULT_METHOD_WIDTH))
        setColumnPreferredWidth(TimelineTableModel.INDEX_URL, AppPreferences.get(PREFERENCE_KEY_INDEX_URL_WIDTH, DEFAULT_URL_WIDTH))
        setColumnPreferredWidth(TimelineTableModel.INDEX_FORMAT, AppPreferences.get(PREFERENCE_KEY_INDEX_FORMAT_WIDTH, DEFAULT_FORMAT_WIDTH))
        setColumnPreferredWidth(TimelineTableModel.INDEX_STATUS_CODE, AppPreferences.get(PREFERENCE_KEY_INDEX_STATUS_WIDTH, DEFAULT_STATUS_WIDTH))
    }

    private val scrollToEndListener = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            tableView.scrollRectToVisible(tableView.getCellRect(tableView.rowCount - 1, 0, true))
        }
    }

    init {
        add(JBScrollPane(tableView), BorderLayout.CENTER)
    }

    override var urlHider: BaseUrlHider?
        get() = model.urlHider
        set(value) {
            model.urlHider = value
        }

    override fun updateScrollToEnd(scrollToEnd: Boolean) {
        tableView.removeComponentListener(scrollToEndListener)
        if (scrollToEnd)
            tableView.addComponentListener(scrollToEndListener)
    }

    override fun onMessagesUpdated() {
    }

}

class TimelineTableModel(private val messageContainer: NiddlerMessageStorage<ParsedNiddlerMessage>) : AbstractTableModel(), ChronologicalMessagesView.MessagesListener {

    companion object {
        const val INDEX_TIMESTAMP = 0
        const val INDEX_DIRECTION = 1
        const val INDEX_METHOD = 2
        const val INDEX_URL = 3
        const val INDEX_STATUS_CODE = 4
        const val INDEX_FORMAT = 5

        private val empty = ParsedNiddlerMessage(
                NetworkNiddlerMessage(
                        requestId = "",
                        messageId = "",
                        timestamp = 0
                ),
                BodyFormat.NONE,
                bodyData = null,
                parsedNetworkReply = null,
                parsedNetworkRequest = null
        )
    }

    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    var urlHider: BaseUrlHider? = null
        set(value) {
            field = value
            fireTableDataChanged()
        }

    private val upIcon = loadIcon("/ic_up.png")
    private val downIcon = loadIcon("/ic_down.png")
    private var messages = messageContainer.messagesChronological.newView(filter = null, messageListener = this)

    fun getMessageAtRow(rowIndex: Int): ParsedNiddlerMessage? {
        return messages[rowIndex]
    }

    override fun getRowCount(): Int = messages.size

    override fun getColumnCount(): Int = 6

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val message = messages[rowIndex] ?: return empty
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

    override fun onChanged() {
        dispatchMain { fireTableDataChanged() }
    }

    override fun onItemAdded(index: Int) {
        dispatchMain { fireTableRowsInserted(index, index) }
    }

    override fun onCleared() {
        dispatchMain { fireTableDataChanged() }
    }

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