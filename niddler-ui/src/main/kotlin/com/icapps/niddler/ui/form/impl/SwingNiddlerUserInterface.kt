package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.HintTextField
import com.icapps.niddler.ui.form.components.NiddlerToolbar
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.impl.SwingToolbar
import com.icapps.niddler.ui.form.ui.NiddlerDetailUserInterface
import com.icapps.niddler.ui.form.ui.NiddlerOverviewUserInterface
import com.icapps.niddler.ui.form.ui.NiddlerUserInterface
import com.icapps.niddler.ui.model.MessageContainer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
open class SwingNiddlerUserInterface(override val componentsFactory: ComponentsFactory) : NiddlerUserInterface, SwingNiddlerOverviewUserInterface.NiddlerOverviewParent {

    override var connectButtonListener: (() -> Unit)? = null
    override var filterListener: ((String?) -> Unit)? = null

    override val asComponent: JComponent
        get() = rootPanel
    override lateinit var toolbar: NiddlerToolbar

    override lateinit var overview: NiddlerOverviewUserInterface
    override lateinit var detail: NiddlerDetailUserInterface

    protected val rootPanel: JPanel

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

    protected open fun uiContainer(): JComponent {
        return rootPanel
    }

    override fun init(messageContainer: MessageContainer) {
        initStatusbar()
        initDetail(messageContainer)
        initOverview()
        initScroller()
        initSplitPane()
        initConnectPanel()
        initToolbar()
    }

    protected open fun initSplitPane() {
        splitPane = componentsFactory.createSplitPane()
        splitPane.resizePriority = 1.0
        uiContainer().add(splitPane.asComponent, BorderLayout.CENTER)
        splitPane.left = messagesScroller
        splitPane.right = detail.asComponent
    }

    protected open fun initScroller() {
        messagesScroller = componentsFactory.createScrollPane()
        messagesScroller.setViewportView(overview.messagesAsTable)
    }

    protected open fun initStatusbar() {
        statusBar = JPanel()
        statusBar.layout = BorderLayout(0, 0)
        uiContainer().add(statusBar, BorderLayout.SOUTH)
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

    protected open fun initConnectPanel() {
        val panel1 = JPanel()
        panel1.layout = BorderLayout(5, 5)
        uiContainer().add(panel1, BorderLayout.NORTH)
        connectButton = JButton()
        connectButton.text = "Connect"
        panel1.add(connectButton, BorderLayout.WEST)

        connectButton.addActionListener { connectButtonListener?.invoke() }

        initFilter(panel1)
    }

    protected open fun initFilter(parent: JPanel) {
        val filter = HintTextField()
        filter.hint = "Filter"
        filter.preferredSize = Dimension(200, filter.preferredSize.height)
        filter.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                onChange()
            }

            override fun removeUpdate(e: DocumentEvent) {
                onChange()
            }

            override fun changedUpdate(e: DocumentEvent) {
                onChange()
            }

            private fun onChange() {
                filterListener?.invoke(filter.text)
            }
        })

        parent.add(filter, BorderLayout.EAST)
    }

    protected open fun initToolbar() {
        toolbar = SwingToolbar(uiContainer())
    }

    override fun setStatusText(statusText: String?) {
        this.statusText.text = statusText
    }

    override fun setStatusIcon(icon: ImageIcon?) {
        this.statusText.icon = icon
    }

    protected open fun initDetail(messagesContainer: MessageContainer) {
        detail = SwingNiddlerDetailUserInterface(componentsFactory, messagesContainer)
        detail.init()
    }

    protected open fun initOverview() {
        overview = SwingNiddlerOverviewUserInterface(this)
        overview.init()
    }

    override fun showTable() {
        messagesScroller.setViewportView(overview.messagesAsTable)
    }

    override fun showLinked() {
        messagesScroller.setViewportView(overview.messagesAsTree)
    }
}