package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.model.ui.LinkedMessagesRenderer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Insets
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

        buttonTimeline = JToggleButton()
        buttonTimeline.isFocusPainted = true
        buttonTimeline.icon = ImageIcon(javaClass.getResource("/ic_chronological.png"))
        buttonTimeline.inheritsPopupMenu = false
        buttonTimeline.margin = Insets(0, 2, 0, 2)
        buttonTimeline.maximumSize = Dimension(32, 32)
        buttonTimeline.minimumSize = Dimension(32, 32)
        buttonTimeline.preferredSize = Dimension(32, 32)
        buttonTimeline.isSelected = true
        buttonTimeline.text = ""
        toolbar.add(buttonTimeline)

        buttonLinkedMode = JToggleButton()
        buttonLinkedMode.isFocusPainted = true
        buttonLinkedMode.icon = ImageIcon(javaClass.getResource("/ic_link.png"))
        buttonLinkedMode.margin = Insets(0, 2, 0, 2)
        buttonLinkedMode.maximumSize = Dimension(32, 32)
        buttonLinkedMode.minimumSize = Dimension(32, 32)
        buttonLinkedMode.preferredSize = Dimension(32, 32)
        buttonLinkedMode.text = ""
        toolbar.add(buttonLinkedMode)

        buttonClear = JButton()
        buttonClear.icon = ImageIcon(javaClass.getResource("/ic_delete.png"))
        buttonClear.text = ""
        buttonClear.maximumSize = Dimension(32, 32)
        buttonClear.minimumSize = Dimension(32, 32)
        buttonClear.preferredSize = Dimension(32, 32)
        toolbar.add(buttonClear)

        splitPane = factory.createSplitPane()
        splitPane.resizePriority = 1.0
        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)

        messagesScroller = JScrollPane()
        splitPane.left = messagesScroller
        messagesAsTable = JTable()
        messagesAsTable.fillsViewportHeight = false
        messagesAsTable.rowHeight = 32
        messagesAsTable.showHorizontalLines = true
        messagesAsTable.showVerticalLines = false
        messagesScroller.setViewportView(messagesAsTable)

        messagesAsTree = JTree()
        messagesAsTree.rowHeight = 32
        messagesAsTree.isEditable = false
        messagesAsTree.dragEnabled = false
        messagesAsTree.isRootVisible = false
        messagesAsTree.showsRootHandles = true
        messagesAsTree.cellRenderer = LinkedMessagesRenderer()

        statusBar = JPanel()
        statusBar.layout = BorderLayout(0, 0)
        rootPanel.add(statusBar, BorderLayout.SOUTH)
        statusBar.border = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null)
        statusText = JLabel()
        statusText.isFocusable = false
        statusText.text = ""
        statusText.verifyInputWhenFocusTarget = false
        statusText.putClientProperty("html.disable", java.lang.Boolean.FALSE)
        statusBar.add(statusText, BorderLayout.CENTER)

        val buttonGroup: ButtonGroup = ButtonGroup()
        buttonGroup.add(buttonTimeline)
        buttonGroup.add(buttonLinkedMode)
    }

}
