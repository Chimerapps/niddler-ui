package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.HintTextField
import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.impl.SwingToolbar
import com.icapps.niddler.ui.form.ui.NiddlerDetailUserInterface
import com.icapps.niddler.ui.form.ui.NiddlerOverviewUserInterface
import com.icapps.niddler.ui.form.ui.NiddlerStatusbar
import com.icapps.niddler.ui.form.ui.NiddlerUserInterface
import com.icapps.niddler.ui.model.MessageContainer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
open class SwingNiddlerUserInterface(override val componentsFactory: ComponentsFactory) : NiddlerUserInterface, SwingNiddlerOverviewUserInterface.NiddlerOverviewParent {

    override var connectButtonListener: (() -> Unit)? = null
    override var filterListener: ((String?) -> Unit)? = null
    override var disconnectButtonListener: (() -> Unit)? = null

    override val asComponent: JComponent
        get() = rootPanel
    override lateinit var toolbar: NiddlerMainToolbar

    override lateinit var overview: NiddlerOverviewUserInterface
    override lateinit var detail: NiddlerDetailUserInterface
    override lateinit var disconnectButton: Component
    override lateinit var statusBar: NiddlerStatusbar

    protected val rootPanel: JPanel

    private lateinit var splitPane: SplitPane
    private lateinit var connectButton: JButton
    private lateinit var messagesScroller: JScrollPane


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
        val statusBar = SwingNiddlerStatusbar()
        uiContainer().add(statusBar.statusBar, BorderLayout.SOUTH)
        this.statusBar = statusBar
    }

    protected open fun initConnectPanel() {
        val topPanel = JPanel()
        topPanel.layout = BorderLayout(5, 5)
        uiContainer().add(topPanel, BorderLayout.NORTH)

        val buttonsPanel = JPanel().apply { layout = FlowLayout(FlowLayout.LEFT, 0, 0) }
        topPanel.add(buttonsPanel, BorderLayout.WEST)

        connectButton = JButton()
        connectButton.text = "Connect"

        connectButton.addActionListener { connectButtonListener?.invoke() }

        disconnectButton = JButton().apply {
            text = "Disconnect"
            isEnabled = false
            addActionListener { disconnectButtonListener?.invoke() }
        }

        buttonsPanel.add(connectButton)
        buttonsPanel.add(disconnectButton)

        initFilter(topPanel)
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