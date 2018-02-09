package com.icapps.niddler.ui.form.debug.impl

import com.icapps.niddler.ui.button
import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.debug.ConfigurationModel
import com.icapps.niddler.ui.form.debug.DebugToolbar
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.content.BlacklistPanel
import com.icapps.niddler.ui.form.debug.content.ContentPanel
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
import javax.swing.*
import javax.swing.tree.TreeSelectionModel

/**
 * @author nicolaverbeeck
 */
open class SwingNiddlerDebugConfigurationDialog(parent: Window?,
                                                private val factory: ComponentsFactory,
                                                initialConfig: DebuggerConfiguration)

    : NiddlerDebugConfigurationDialog, JDialog(parent) {

    override var visibility: Boolean
        get() = super.isVisible()
        set(value) = super.setVisible(value)

    private var isChanged: Boolean = false
        set(value) {
            field = value
            applyButton.isEnabled = field
        }

    override lateinit var debugToolbar: DebugToolbar
    override lateinit var configurationTree: JTree
    override lateinit var detailPanelContainer: JPanel
    override lateinit var configurationModel: ConfigurationModel

    protected var changingConfiguration = TemporaryDebuggerConfiguration(initialConfig)

    protected val rootPanel: JPanel = JPanel(BorderLayout())
    protected val splitPane: SplitPane = factory.createSplitPane()

    protected var currentDetailPanelType: CurrentDetailPanelType = CurrentDetailPanelType.BLACKLIST
    protected var currentDetailPanel: ContentPanel? = null
    protected var currentDetailPayload: Any? = null

    protected lateinit var applyListener: (DebuggerConfiguration) -> Unit
    protected lateinit var applyButton: JComponent

    override fun init(applyListener: (DebuggerConfiguration) -> Unit) {
        this.applyListener = applyListener
        contentPane = rootPanel

        isModal = true

        configurationModel = ConfigurationModel { isChanged = true }
        configurationTree = initConfigurationTree()
        initTreeListener()

        splitPane.left = factory.createScrollPane().apply { setViewportView(configurationTree) }
        splitPane.right = BlacklistPanel(changingConfiguration)

        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)

        createActions()
        createButtons()

        setSize(600, 300)
        if (parent != null)
            setLocationRelativeTo(parent)

        configurationModel.setDelaysEnabled(changingConfiguration.delayConfiguration.enabled)
        isChanged = false
    }

    protected open fun createActions() {
        val debugToolbar = SwingDebugToolbar()
        rootPanel.add(debugToolbar, BorderLayout.NORTH)
        this.debugToolbar = debugToolbar
    }

    protected open fun createButtons() {
        val buttonPanel = Box.createHorizontalBox()
        buttonPanel.add(Box.createHorizontalGlue())
        buttonPanel += button("Cancel") { onCancel() }
        applyButton = button("Apply") { onApply() }
        buttonPanel += applyButton
        buttonPanel += button("Apply and close") {
            onApply()
            visibility = false
        }
        rootPanel.add(buttonPanel, BorderLayout.SOUTH)
    }

    protected open fun initConfigurationTree(): JTree {
        return JTree(configurationModel.treeModel).apply {

            showsRootHandles = true
            cellRenderer = CheckedCellRenderer(DefaultCellRenderer())
            cellEditor = CheckboxCellEditor(cellRenderer as CheckedCellRenderer, this)
            isEditable = true
            isRootVisible = false

            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        }
    }

    protected open fun initTreeListener() {
        configurationTree.addTreeSelectionListener { _ ->
            val component = configurationTree.lastSelectedPathComponent
            when (component) {
                is TimeoutConfigurationRootNode -> onTimeoutNodeSelected()
                is BlacklistRootNode -> onBlacklistRootNodeSelected()
                is BlacklistNode -> onBlacklistNodeSelected(component.regex)
            }
        }
    }

    protected open fun onTimeoutNodeSelected() {
        if (currentDetailPanelType == CurrentDetailPanelType.DELAYS)
            return

        currentDetailPanel?.apply(currentNodeEnabled())
        currentDetailPanelType = CurrentDetailPanelType.DELAYS
        val right = DelaysConfigurationPanel(changingConfiguration) {
            isChanged = true
        }
        currentDetailPanel = right
        splitPane.right = right
    }

    protected open fun onBlacklistRootNodeSelected() {
        return
    }

    protected open fun onBlacklistNodeSelected(regex: String) {
        if (currentDetailPanelType == CurrentDetailPanelType.BLACKLIST && currentDetailPayload == regex)
            return

        currentDetailPanel?.apply(currentNodeEnabled())

        currentDetailPanelType = CurrentDetailPanelType.BLACKLIST
        currentDetailPayload = regex

        val right = BlacklistPanel(changingConfiguration)
        currentDetailPanel = right
        splitPane.right = right.apply {
            init(changingConfiguration.blacklistConfiguration.first { it.item == regex })
        }
    }

    protected open fun onCancel() {
        visibility = false
    }

    protected open fun onApply() {
        currentDetailPanel?.apply(currentNodeEnabled())
        isChanged = false
        applyListener(changingConfiguration)
    }

    protected open fun currentNodeEnabled(): Boolean {
        return when (currentDetailPanelType) {
            SwingNiddlerDebugConfigurationDialog.CurrentDetailPanelType.DELAYS ->
                configurationModel.isDelaysEnabled()
            SwingNiddlerDebugConfigurationDialog.CurrentDetailPanelType.BLACKLIST ->
                configurationModel.isBlacklistEnabled(currentDetailPayload as? String?)
        }
    }

    enum class CurrentDetailPanelType {
        DELAYS,
        BLACKLIST
    }

}