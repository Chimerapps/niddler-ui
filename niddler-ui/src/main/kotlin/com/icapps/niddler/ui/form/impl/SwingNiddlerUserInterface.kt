package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.NiddlerUserInterface
import com.icapps.niddler.ui.form.PopupMenuSelectingJTable
import com.icapps.niddler.ui.form.components.NiddlerToolbar
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.impl.SwingToolbar
import com.icapps.niddler.ui.model.ui.LinkedMessagesRenderer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
open class SwingNiddlerUserInterface(override val componentsFactory: ComponentsFactory) : NiddlerUserInterface {

    override var connectButtonListener: (() -> Unit)? = null

    override lateinit var messagesAsTable: JTable
    override lateinit var messagesAsTree: JTree
    override val asComponent: JComponent
        get() = rootPanel
    override lateinit var toolbar: NiddlerToolbar

    private val rootPanel: JPanel

    private lateinit var splitPane: SplitPane
    private lateinit var statusBar: JPanel
    private lateinit var connectButton: JButton
    private lateinit var messagesScroller: JScrollPane

    private lateinit var statusText: JLabel

    init {
        rootPanel = JPanel()
        rootPanel.layout = BorderLayout(0, 0)
        rootPanel.minimumSize = Dimension(300, 300)
    }

    override fun init() {
        initStatusbar()
        initMessagesWindows()
        initScroller()
        initSplitPane()
        initConnectPanel()
        initToolbar()
    }

    protected open fun initSplitPane() {
        splitPane = componentsFactory.createSplitPane()
        splitPane.resizePriority = 1.0
        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)
        splitPane.left = messagesScroller
    }

    protected open fun initScroller() {
        messagesScroller = componentsFactory.createScrollPane()
        messagesScroller.setViewportView(messagesAsTable)
    }

    protected open fun initStatusbar() {
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
        statusBar.border = BorderFactory.createCompoundBorder(statusBar.border, EmptyBorder(1, 6, 1, 6))
    }

    protected open fun initMessagesWindows() {
        messagesAsTable = PopupMenuSelectingJTable().apply {
            fillsViewportHeight = false
            rowHeight = 32
            showHorizontalLines = true
            showVerticalLines = false
        }

        messagesAsTree = JTree().apply {
            rowHeight = 32
            isEditable = false
            dragEnabled = false
            isRootVisible = false
            showsRootHandles = true
            cellRenderer = LinkedMessagesRenderer(0)
        }
    }

    protected open fun initConnectPanel() {
        val panel1 = JPanel()
        panel1.layout = FlowLayout(FlowLayout.LEFT, 5, 5)
        rootPanel.add(panel1, BorderLayout.NORTH)
        connectButton = JButton()
        connectButton.text = "Connect"
        panel1.add(connectButton)

        connectButton.addActionListener { connectButtonListener?.invoke() }
    }

    protected open fun initToolbar() {
        toolbar = SwingToolbar(rootPanel)
    }

    override fun setStatusText(statusText: String?) {
        this.statusText.text = statusText
    }

    override fun setStatusIcon(icon: ImageIcon?) {
        this.statusText.icon = icon
    }

}