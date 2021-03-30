package com.chimerapps.niddler.ui.component.renderer

import com.chimerapps.niddler.ui.component.view.LinkedResponseNode
import com.chimerapps.niddler.ui.component.view.LinkedRootNode
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.icapps.niddler.lib.model.NiddlerMessageType
import com.icapps.niddler.lib.utils.getStatusCodeString
import com.intellij.ui.components.JBLabel
import com.intellij.ui.render.LabelBasedRenderer
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Dimension
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingConstants

class LinkedViewCellRenderer : LabelBasedRenderer.Tree() {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val directionUp = IncludedIcons.Status.outgoing
    private val directionDown = IncludedIcons.Status.incoming
    private val directionDownCached = IncludedIcons.Status.incoming_cached
    private val directionUpDebug = IncludedIcons.Status.outgoing_debugged
    private val directionDownDebug = IncludedIcons.Status.incoming_debugged
    private val timeCache = Date()

    private var myFocusedCalculated = false
    private var myFocused = false
    private lateinit var myTree: JTree
    private var mySelected = false

    private val requestTimeLabel = JBLabel()
    private val methodLabel = JBLabel()
    private val requestDirectionLabel = JBLabel()
    private val urlLabel = JBLabel()
    private val requestPanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.LINE_AXIS)
        it.add(requestTimeLabel)
        it.add(requestDirectionLabel)
        it.add(methodLabel)
        it.add(urlLabel)

        requestDirectionLabel.icon = directionUp

        urlLabel.border = BorderFactory.createEmptyBorder(0, 5, 0, 0)

        requestTimeLabel.horizontalAlignment = SwingConstants.LEFT
        requestDirectionLabel.horizontalAlignment = SwingConstants.CENTER
        methodLabel.horizontalAlignment = SwingConstants.CENTER
        urlLabel.horizontalAlignment = SwingConstants.LEFT

        requestTimeLabel.setFixedWidth(90)
        requestDirectionLabel.setFixedWidth(36)
        methodLabel.preferredSize = Dimension(100, 32)
    }

    private val responseTimeLabel = JBLabel()
    private val responseDirectionLabel = JBLabel()
    private val responseStatusLabel = JBLabel()
    private val responseTypeLabel = JBLabel()
    private val responsePanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.LINE_AXIS)
        it.add(responseTimeLabel)
        it.add(responseDirectionLabel)
        it.add(responseStatusLabel)
        it.add(responseTypeLabel)

        responseDirectionLabel.icon = directionDown

        responseTimeLabel.horizontalAlignment = SwingConstants.LEFT
        responseDirectionLabel.horizontalAlignment = SwingConstants.CENTER
        responseStatusLabel.horizontalAlignment = SwingConstants.LEFT
        responseTypeLabel.horizontalAlignment = SwingConstants.LEFT

        responseTimeLabel.setFixedWidth(90)
        responseDirectionLabel.setFixedWidth(36)
        responseStatusLabel.setFixedWidth(70)
    }

    override fun getTreeCellRendererComponent(tree: JTree, value: Any?, selected: Boolean,
                                              expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        myTree = tree
        mySelected = selected
        myFocusedCalculated = false

        when (value) {
            is LinkedRootNode -> return getRequestComponent(value.entry.request, selected, hasFocus)
            is LinkedResponseNode -> return getResponseComponent(value, selected, hasFocus)
        }
        return this
    }

    private fun getRequestComponent(request: NiddlerMessageInfo?, selected: Boolean, hasFocus: Boolean): Component {
        updatePanel(requestPanel, myTree, selected, hasFocus)

        if (request == null) {
            requestTimeLabel.text = ""
            methodLabel.text = ""
            urlLabel.text = ""
            requestDirectionLabel.icon = null
        } else {
            requestTimeLabel.text = formatTime(request.timestamp)
            methodLabel.text = request.method
            urlLabel.text = request.url
            requestDirectionLabel.icon = getIcon(request)

            updateForeground(requestTimeLabel)
            updateForeground(methodLabel)
            updateForeground(urlLabel)
        }
        requestPanel.revalidate()
        requestPanel.repaint()

        return requestPanel
    }

    private fun getResponseComponent(value: LinkedResponseNode, selected: Boolean, hasFocus: Boolean): Component {
        if (value.response.isRequest)
            return getRequestComponent(value.response, selected, hasFocus)

        updatePanel(responsePanel, myTree, selected, hasFocus)

        val response = value.response
        responseTimeLabel.text = formatTime(response.timestamp)
        responseStatusLabel.text = formatStatusCode(response.statusCode)

        responseTypeLabel.text = response.format ?: ""

        responseDirectionLabel.icon = getIcon(response)

        updateForeground(responseTimeLabel)
        updateForeground(responseStatusLabel)
        updateForeground(responseTypeLabel)

        responsePanel.revalidate()
        responsePanel.repaint()
        return responsePanel
    }

    private fun getIcon(message: NiddlerMessageInfo): Icon {
        return when (message.type) {
            NiddlerMessageType.UP -> directionUp
            NiddlerMessageType.DOWN -> directionDown
            NiddlerMessageType.UP_DEBUG -> directionUpDebug
            NiddlerMessageType.DOWN_DEBUG -> directionDownDebug
            NiddlerMessageType.DOWN_CACHED -> directionDownCached
        }
    }

    private fun updatePanel(panel: JPanel, tree: JTree, selected: Boolean, hasFocus: Boolean) {
        if (UIUtil.isFullRowSelectionLAF()) {
            panel.background = if (selected) UIUtil.getTreeSelectionBackground(true) else null
        } else if (selected) {
            if (isFocused()) {
                panel.background = UIUtil.getTreeSelectionBackground(true)
            } else {
                panel.background = null
            }
        } else {
            panel.background = null
        }

        panel.foreground = tree.foreground

        panel.isOpaque = selected && hasFocus || selected && isFocused() // draw selection background even for non-opaque tree
    }

    private fun updateForeground(label: JBLabel) {
        if (mySelected && isFocused()) {
            label.foreground = UIUtil.getTreeForeground(true, true)
        } else if (mySelected && UIUtil.isUnderAquaBasedLookAndFeel()) {
            label.foreground = UIUtil.getTreeForeground()
        } else {
            label.foreground = foreground
        }
    }

    private fun isFocused(): Boolean {
        if (!myFocusedCalculated) {
            myFocused = calcFocusedState()
            myFocusedCalculated = true
        }
        return myFocused
    }

    private fun calcFocusedState(): Boolean {
        return myTree.hasFocus()
    }

    private fun formatTime(timestamp: Long): String {
        timeCache.time = timestamp
        return timeFormatter.format(timeCache)
    }

    private fun formatStatusCode(statusCode: Int?): String {
        return if (statusCode == null) {
            ""
        } else {
            String.format("%d - %s", statusCode, getStatusCodeString(statusCode))
        }
    }
}


private fun JComponent.setFixedWidth(width: Int) {
    minimumSize = Dimension(width, 32)
    maximumSize = Dimension(width, 32)
    preferredSize = Dimension(width, 32)
}