package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.setFixedWidth
import com.icapps.niddler.ui.util.getStatusCodeString
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.tree.TreeCellRenderer


/**
 * @author Nicola Verbeeck
 * @date 02/05/2017.
 */
class LinkedMessagesRenderer : TreeCellRenderer {

    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val upIcon: Icon
    private val downIcon: Icon

    init {
        upIcon = ImageIcon(javaClass.getResource("/ic_up.png"))
        downIcon = ImageIcon(javaClass.getResource("/ic_down.png"))
    }

    private val requestFrame = JPanel()
    private val timeLabel = JLabel()
    private val directionLabel = JLabel()
    private val methodLabel = JLabel()
    private val urlLabel = JLabel()
    private val statusLabel = JLabel()
    private val formatLabel = JLabel()
    private val selectionTextColor: Color by lazy {
        UIManager.getColor("Table.selectionForeground") ?: UIManager.getColor("textHighlightText")
    }
    private val selectionBackgroundColor: Color by lazy {
        UIManager.getColor("Table.selectionBackground") ?: UIManager.getColor("textHighlight")
    }
    private val defaultForeground: Color

    init {
        requestFrame.layout = BoxLayout(requestFrame, BoxLayout.LINE_AXIS)
        requestFrame.background = null
        requestFrame.add(timeLabel)
        requestFrame.add(directionLabel)
        requestFrame.add(methodLabel)
        requestFrame.add(urlLabel)
        requestFrame.add(statusLabel)
        requestFrame.add(formatLabel)

        timeLabel.horizontalAlignment = SwingConstants.LEFT
        directionLabel.horizontalAlignment = SwingConstants.CENTER
        methodLabel.horizontalAlignment = SwingConstants.CENTER
        urlLabel.horizontalAlignment = SwingConstants.LEFT

        defaultForeground = timeLabel.foreground
        timeLabel.setFixedWidth(90)
        directionLabel.setFixedWidth(36)
    }

    override fun getTreeCellRendererComponent(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        var message: ParsedNiddlerMessage? = null
        var icon: Icon? = null
        var method: String? = null
        var url: String? = null
        var status: String? = null
        var format: String? = null
        if (value is RequestNode) {
            message = value.request
            icon = upIcon
            method = message?.method
            url = message?.url

            methodLabel.setFixedWidth(70)
            urlLabel.setFixedWidth(400)
            statusLabel.setFixedWidth(0)
        } else if (value is ResponseNode) {
            message = value.message
            icon = downIcon
            status = formatStatusCode(value.message.statusCode)
            format = value.message.bodyFormat.toString()

            methodLabel.setFixedWidth(0)
            urlLabel.setFixedWidth(0)
            statusLabel.setFixedWidth(70)
        }

        if (message != null) {
            timeLabel.text = formatter.format(Date(message.timestamp))
            directionLabel.icon = icon
            methodLabel.text = method
            urlLabel.text = url
            statusLabel.text = status
            formatLabel.text = format
        }
        if (selected) {
            requestFrame.background = selectionBackgroundColor
            setLabelForeground(selectionTextColor)
        } else {
            requestFrame.background = null
            setLabelForeground(defaultForeground)
        }

        return requestFrame
    }

    private fun formatStatusCode(statusCode: Int?): String {
        return if (statusCode == null) {
            ""
        } else {
            String.format("%d %s", statusCode, getStatusCodeString(statusCode))
        }
    }

    private fun setLabelForeground(color: Color) {
        timeLabel.foreground = color
        methodLabel.foreground = color
        urlLabel.foreground = color
        statusLabel.foreground = color
        formatLabel.foreground = color
    }

}