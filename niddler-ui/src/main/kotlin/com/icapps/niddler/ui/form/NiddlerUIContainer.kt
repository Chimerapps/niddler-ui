package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.form.components.SplitPane
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
    val messages: JTable
    private val toolbar: JToolBar
    private val buttonTimeline: JToggleButton
    private val buttonLinkedMode: JToggleButton
    val buttonClear: JButton
    val splitPane: SplitPane
    val statusText: JLabel
    val statusBar: JPanel
    val connectButton: JButton

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

        val scrollPane1 = JScrollPane()
        splitPane.left = scrollPane1
        messages = JTable()
        messages.fillsViewportHeight = false
        messages.rowHeight = 32
        messages.showHorizontalLines = true
        messages.showVerticalLines = false
        scrollPane1.setViewportView(messages)

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

        val buttonGroup: ButtonGroup
        buttonGroup = ButtonGroup()
        buttonGroup.add(buttonTimeline)
        buttonGroup.add(buttonLinkedMode)
    }

}
