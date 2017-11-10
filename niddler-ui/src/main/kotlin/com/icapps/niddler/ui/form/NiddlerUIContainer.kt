package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.model.ui.LinkedMessagesRenderer
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * @author Nicola Verbeeck
 * *
 * @date 10/11/16.
 */
internal class NiddlerUIContainer(factory: InterfaceFactory) {

    val rootPanel: JPanel
    val messagesAsTable: JTable
    val messagesAsTree: JTree
    private val toolbar: JToolBar
    val buttonTimeline: JToggleButton
    val buttonLinkedMode: JToggleButton
    val buttonClear: JButton
    val buttonExport: JButton

    val splitPane: SplitPane
    val statusText: JLabel
    val statusBar: JPanel
    val connectButton: JButton
    val messagesScroller: JScrollPane

    init {
        rootPanel = JPanel()
        rootPanel.layout = BorderLayout(0, 0)
        rootPanel.minimumSize = Dimension(300, 300)

        val panel1 = JPanel()
        panel1.layout = FlowLayout(FlowLayout.LEFT, 5, 5)
        rootPanel.add(panel1, BorderLayout.NORTH)
        connectButton = JButton()
        connectButton.text = "Connect"
        panel1.add(connectButton)

        val panel2 = JPanel()
        panel2.layout = BorderLayout(0, 0)
        rootPanel.add(panel2, BorderLayout.WEST)
        toolbar = JToolBar()
        toolbar.isFloatable = false
        toolbar.margin = Insets(0, 4, 0, 4)
        toolbar.orientation = 1
        panel2.add(toolbar, BorderLayout.NORTH)

        buttonTimeline = JToggleButton().apply {
            isFocusPainted = true
            icon = ImageIcon(NiddlerUIContainer::class.java.getResource("/ic_chronological.png"))
            inheritsPopupMenu = false
            margin = Insets(0, 2, 0, 2)
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
            isSelected = true
            text = ""
        }
        toolbar.add(buttonTimeline)

        buttonLinkedMode = JToggleButton().apply {
            isFocusPainted = true
            icon = ImageIcon(NiddlerUIContainer::class.java.getResource("/ic_link.png"))
            margin = Insets(0, 2, 0, 2)
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
            text = ""
        }
        toolbar.add(buttonLinkedMode)

        buttonClear = JButton().apply {
            icon = ImageIcon(NiddlerUIContainer::class.java.getResource("/ic_delete.png"))
            text = ""
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
        }
        toolbar.addSeparator()
        toolbar.add(buttonClear)

        buttonExport = JButton().apply {
            icon = ImageIcon(NiddlerUIContainer::class.java.getResource("/ic_save.png"))
            text = ""
            maximumSize = Dimension(32, 32)
            minimumSize = Dimension(32, 32)
            preferredSize = Dimension(32, 32)
        }
        toolbar.addSeparator()
        toolbar.add(buttonExport)

        splitPane = factory.createSplitPane()
        splitPane.resizePriority = 1.0
        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)

        messagesScroller = factory.createScrollPane()
        splitPane.left = messagesScroller
        messagesAsTable = PopupMenuSelectingJTable().apply {
            fillsViewportHeight = false
            rowHeight = 32
            showHorizontalLines = true
            showVerticalLines = false
        }
        messagesScroller.setViewportView(messagesAsTable)

        messagesAsTree = JTree().apply {
            rowHeight = 32
            isEditable = false
            dragEnabled = false
            isRootVisible = false
            showsRootHandles = true
            cellRenderer = LinkedMessagesRenderer(0)
        }

        statusBar = JPanel()
        statusBar.layout = BorderLayout(0, 0)
        rootPanel.add(statusBar, BorderLayout.SOUTH)
        statusBar.border = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null)
        statusText = JLabel().apply {
            isFocusable = false
            text = ""
            verifyInputWhenFocusTarget = false
            putClientProperty("html.disable", java.lang.Boolean.FALSE)
        }
        statusBar.add(statusText, BorderLayout.CENTER)

        val buttonGroup: ButtonGroup = ButtonGroup()
        buttonGroup.add(buttonTimeline)
        buttonGroup.add(buttonLinkedMode)
    }

    fun updateProtocol(protocol: Int) {
        messagesAsTree.cellRenderer = LinkedMessagesRenderer(protocol)
    }

}

class PopupMenuSelectingJTable : JTable() {

    override fun getPopupLocation(event: MouseEvent): Point? {
        val r = rowAtPoint(event.point);
        if (r in 0..(rowCount - 1)) {
            setRowSelectionInterval(r, r);
        }
        return super.getPopupLocation(event)
    }
}
