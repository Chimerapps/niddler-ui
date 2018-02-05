package com.icapps.niddler.ui.form.debug.impl

import com.icapps.niddler.ui.button
import com.icapps.niddler.ui.debugger.model.DebuggerInterface
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.debug.ConfigurationModel
import com.icapps.niddler.ui.form.debug.DebugToolbar
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.content.BlacklistPanel
import com.icapps.niddler.ui.form.debug.content.DelaysConfigurationPanel
import com.icapps.niddler.ui.form.debug.nodes.BlacklistNode
import com.icapps.niddler.ui.form.debug.nodes.BlacklistRootNode
import com.icapps.niddler.ui.form.debug.nodes.TimeoutConfigurationRootNode
import com.icapps.niddler.ui.form.debug.nodes.renderer.CheckboxCellEditor
import com.icapps.niddler.ui.form.debug.nodes.renderer.CheckedCellRenderer
import com.icapps.niddler.ui.form.debug.nodes.renderer.DefaultCellRenderer
import com.icapps.niddler.ui.plusAssign
import java.awt.BorderLayout
import java.awt.Window
import javax.swing.Box
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.TreeSelectionModel

/**
 * @author nicolaverbeeck
 */
open class SwingNiddlerDebugConfigurationDialog(parent: Window?,
                                                private val factory: ComponentsFactory,
                                                private val debuggerInterface: DebuggerInterface)
    : NiddlerDebugConfigurationDialog, JDialog(parent) {

    override var visibility: Boolean
        get() = super.isVisible()
        set(value) = super.setVisible(value)

    override lateinit var debugToolbar: DebugToolbar
    override lateinit var configurationTree: JTree
    override lateinit var detailPanelContainer: JPanel
    override lateinit var configurationModel: ConfigurationModel

    protected val rootPanel: JPanel = JPanel(BorderLayout())
    protected val splitPane: SplitPane = factory.createSplitPane()

    protected var currentDetailPanel: CurrentDetailPanel = CurrentDetailPanel.BLACKLIST
    protected var currentDetailPayload: Any? = null

    override fun init() {
        contentPane = rootPanel

        isModal = true

        configurationModel = ConfigurationModel()
        configurationTree = initConfigurationTree()
        initTreeListener()

        splitPane.left = factory.createScrollPane().apply { setViewportView(configurationTree) }
        splitPane.right = BlacklistPanel()

        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)

        createActions()
        createButtons()

        setSize(600, 300)
        if (parent != null)
            setLocationRelativeTo(parent)
    }

    protected fun createActions() {
        val debugToolbar = SwingDebugToolbar()
        rootPanel.add(debugToolbar, BorderLayout.NORTH)
        this.debugToolbar = debugToolbar
    }

    protected fun createButtons() {
        val buttonPanel = Box.createHorizontalBox()
        buttonPanel.add(Box.createHorizontalGlue())
        buttonPanel += button("Cancel") { onCancel() }
        buttonPanel += button("Apply") { onApply() }
        buttonPanel += button("Apply and close") {
            onApply()
            visibility = false
        }
        rootPanel.add(buttonPanel, BorderLayout.SOUTH)
    }

    protected fun initConfigurationTree(): JTree {
        return JTree(configurationModel.treeModel).apply {

            showsRootHandles = true
            cellRenderer = CheckedCellRenderer(DefaultCellRenderer())
            cellEditor = CheckboxCellEditor(cellRenderer as CheckedCellRenderer, this)
            isEditable = true
            isRootVisible = false

            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        }
    }

    protected fun initTreeListener() {
        configurationTree.addTreeSelectionListener { _ ->
            val component = configurationTree.lastSelectedPathComponent
            when (component) {
                is TimeoutConfigurationRootNode -> onTimeoutNodeSelected()
                is BlacklistRootNode -> onBlacklistRootNodeSelected()
                is BlacklistNode -> onBlacklistNodeSelected(component.regex)
            }
        }
    }

    protected fun onTimeoutNodeSelected() {
        if (currentDetailPanel == CurrentDetailPanel.TIMEOUT)
            return
        currentDetailPanel = CurrentDetailPanel.TIMEOUT
        splitPane.right = DelaysConfigurationPanel(debuggerInterface)
    }

    protected fun onBlacklistRootNodeSelected() {
        return
    }

    protected fun onBlacklistNodeSelected(regex: String) {
        if (currentDetailPanel == CurrentDetailPanel.BLACKLIST && currentDetailPayload == regex)
            return

        currentDetailPanel = CurrentDetailPanel.BLACKLIST
        currentDetailPayload = regex
        splitPane.right = BlacklistPanel().apply { init(regex) }
    }

    protected fun onCancel() {
        visibility = false
    }

    protected fun onApply() {

    }

    enum class CurrentDetailPanel {
        TIMEOUT,
        BLACKLIST
    }

}