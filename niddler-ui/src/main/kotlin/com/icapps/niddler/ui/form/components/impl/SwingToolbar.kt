package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.util.loadIcon
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.ItemEvent
import javax.swing.*

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
class SwingToolbar(root: JComponent) : NiddlerMainToolbar {

    override var listener: NiddlerMainToolbar.ToolbarListener? = null
    private val buttonMuteBreakpoints: JButton

    private val buttonDebugView: JToggleButton

    private val debugViewIcon: Icon
    private val debugWarningViewIcon: Icon

    override var hasWaitingBreakpoint: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                buttonDebugView.icon = if (value)
                    debugWarningViewIcon
                else
                    debugViewIcon
            }
        }

    init {
        val actionPanel = JPanel()
        actionPanel.layout = BorderLayout(0, 0)
        root.add(actionPanel, BorderLayout.WEST)

        debugViewIcon = loadIcon("/ic_debug_active.png")
        debugWarningViewIcon = loadIcon("/ic_debug_active_warning.png")

        val toolbar = JToolBar()
        toolbar.isFloatable = false
        toolbar.margin = Insets(0, 4, 0, 4)
        toolbar.orientation = 1
        actionPanel.add(toolbar, BorderLayout.NORTH)

        val buttonTimeline = JToggleButton().apply {
            isFocusPainted = true
            icon = loadIcon("/ic_chronological.png")
            inheritsPopupMenu = false
            margin = Insets(0, 2, 0, 2)
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
            isSelected = true
            text = ""
        }
        toolbar.add(buttonTimeline)

        val buttonLinkedMode = JToggleButton().apply {
            isFocusPainted = true
            icon = loadIcon("/ic_link.png")
            margin = Insets(0, 2, 0, 2)
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
            text = ""
        }
        toolbar.add(buttonLinkedMode)

        buttonDebugView = JToggleButton().apply {
            isFocusPainted = true
            icon = debugViewIcon
            margin = Insets(0, 2, 0, 2)
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
            text = ""
        }
        toolbar.add(buttonDebugView)

        val buttonClear = JButton().apply {
            icon = loadIcon("/ic_delete.png")
            text = ""
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
        }
        toolbar.addSeparator()
        toolbar.add(buttonClear)

        val buttonConfigureBreakpoints = JButton().apply {
            icon = loadIcon("/viewBreakpoints.png")
            text = ""
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
        }
        buttonMuteBreakpoints = JButton().apply {
            icon = loadIcon("/muteBreakpoints.png")
            text = ""
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
        }
        toolbar.addSeparator()
        toolbar.add(buttonConfigureBreakpoints)
        toolbar.add(buttonMuteBreakpoints)

        val buttonExport = JButton().apply {
            icon = loadIcon("/ic_save.png")
            text = ""
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
        }
        toolbar.addSeparator()
        toolbar.add(buttonExport)

        val buttonGroup = ButtonGroup()
        buttonGroup.add(buttonTimeline)
        buttonGroup.add(buttonLinkedMode)
        buttonGroup.add(buttonDebugView)

        buttonTimeline.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                listener?.onTimelineSelected()
            }
        }
        buttonLinkedMode.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                listener?.onLinkedSelected()
            }
        }
        buttonDebugView.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                listener?.onDebuggerViewSelected()
            }
        }
        buttonClear.addActionListener {
            listener?.onClearSelected()
        }
        buttonExport.addActionListener {
            listener?.onExportSelected()
        }
        buttonConfigureBreakpoints.addActionListener {
            listener?.onConfigureBreakpointsSelected()
        }
        buttonMuteBreakpoints.addActionListener {
            listener?.onMuteBreakpointsSelected()
        }
    }

    override fun onBreakpointsMuted(muted: Boolean) {
        buttonMuteBreakpoints.icon = if (muted)
            loadIcon("/muteBreakpoints_muted.png")
        else
            loadIcon("/muteBreakpoints.png")
    }
}