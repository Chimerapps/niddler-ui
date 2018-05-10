package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.MainThreadDispatcher
import com.icapps.niddler.ui.form.components.HintTextField
import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.impl.SwingToolbar
import com.icapps.niddler.ui.form.debug.view.DebugView
import com.icapps.niddler.ui.form.ui.AbstractAction
import com.icapps.niddler.ui.form.ui.NiddlerDetailUserInterface
import com.icapps.niddler.ui.form.ui.NiddlerOverviewUserInterface
import com.icapps.niddler.ui.form.ui.NiddlerStatusbar
import com.icapps.niddler.ui.form.ui.NiddlerUserInterface
import com.icapps.niddler.ui.util.loadIcon
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
open class SwingNiddlerUserInterface(override val componentsFactory: ComponentsFactory)
    : NiddlerUserInterface, SwingNiddlerOverviewUserInterface.NiddlerOverviewParent {

    override var connectButtonListener: (() -> Unit)? = null
    override var filterListener: ((String?) -> Unit)? = null
    override var disconnectButtonListener: (() -> Unit)? = null
    override var debugButtonListener: (() -> Unit)? = null

    override val asComponent: JComponent
        get() = rootPanel
    override lateinit var toolbar: NiddlerMainToolbar

    override lateinit var overview: NiddlerOverviewUserInterface
    override lateinit var detail: NiddlerDetailUserInterface
    override lateinit var statusBar: NiddlerStatusbar

    protected val rootPanel: JPanel

    private lateinit var splitPane: SplitPane
    override lateinit var disconnectButton: AbstractAction
    private lateinit var messagesScroller: JScrollPane
    override lateinit var debugView: DebugView

    init {
        rootPanel = JPanel()
        rootPanel.layout = BorderLayout(0, 0)
        rootPanel.minimumSize = Dimension(300, 300)
    }

    protected open fun uiContainer(): JComponent {
        return rootPanel
    }

    override fun init(messageContainer: NiddlerMessageContainer<ParsedNiddlerMessage>) {
        debugView = DebugView(componentsFactory, ::onDebugMessagesUpdated, messageContainer)
        initStatusbar()
        initDetail(messageContainer.storage)
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

        val toolbar = componentsFactory.createHorizontalToolbar()
        toolbar.addAction(loadIcon("/execute.png"), "Connect without debugger") {
            connectButtonListener?.invoke()
        }
        toolbar.addAction(loadIcon("/startDebugger.png"), "Connect with debugger") {
            debugButtonListener?.invoke()
        }
        disconnectButton = toolbar.addAction(loadIcon("/suspend.png"), "Disconnect") {
            it.isEnabled = false
            disconnectButtonListener?.invoke()
        }

        topPanel.add(toolbar.component, BorderLayout.WEST)

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

    protected open fun initDetail(messagesContainer: NiddlerMessageStorage<ParsedNiddlerMessage>) {
        detail = SwingNiddlerDetailUserInterface(componentsFactory, messagesContainer)
        detail.init()
    }

    protected open fun initOverview() {
        overview = SwingNiddlerOverviewUserInterface(this)
        overview.init()
    }

    override fun showTable() {
        if (splitPane.asComponent.parent == null) {
            uiContainer().let {
                it.remove(debugView)
                it.add(splitPane.asComponent)
                it.invalidate()
                it.repaint()
                it.revalidate()
            }
        }
        messagesScroller.setViewportView(overview.messagesAsTable)
    }

    override fun showLinked() {
        if (splitPane.asComponent.parent == null) {
            uiContainer().let {
                it.remove(debugView)
                it.add(splitPane.asComponent)
                it.invalidate()
                it.repaint()
                it.revalidate()
            }
        }
        messagesScroller.setViewportView(overview.messagesAsTree)
    }

    override fun showDebugView() {
        if (splitPane.asComponent.parent != null) {
            uiContainer().let {
                it.remove(splitPane.asComponent)
                it.add(debugView)
                it.invalidate()
                it.repaint()
                it.revalidate()
            }
        }
    }

    private fun onDebugMessagesUpdated(numItems: Int) {
        MainThreadDispatcher.dispatch {
            toolbar.hasWaitingBreakpoint = (numItems != 0)
        }
    }
}