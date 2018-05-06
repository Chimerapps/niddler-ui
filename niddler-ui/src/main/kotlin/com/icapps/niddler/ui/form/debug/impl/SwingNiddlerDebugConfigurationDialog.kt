package com.icapps.niddler.ui.form.debug.impl

import com.icapps.niddler.lib.debugger.model.LocalRequestOverride
import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.lib.debugger.model.saved.TemporaryDebuggerConfiguration
import com.icapps.niddler.ui.button
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.MainThreadDispatcher
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.debug.ConfigurationModel
import com.icapps.niddler.ui.form.debug.DebugToolbar
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationHelper
import com.icapps.niddler.ui.form.debug.content.BlacklistPanel
import com.icapps.niddler.ui.form.debug.content.ContentPanel
import com.icapps.niddler.ui.form.debug.content.DelaysConfigurationPanel
import com.icapps.niddler.ui.form.debug.content.RequestOverridePanel
import com.icapps.niddler.ui.form.debug.nodes.*
import com.icapps.niddler.ui.form.debug.nodes.swing.SwingNodeBuilder
import com.icapps.niddler.ui.path
import com.icapps.niddler.ui.plusAssign
import org.scijava.swing.checkboxtree.CheckBoxNodeEditor
import org.scijava.swing.checkboxtree.CheckBoxNodeRenderer
import java.awt.BorderLayout
import java.awt.Dimension
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

    protected var changingConfiguration = TemporaryDebuggerConfiguration(initialConfig) {
        isChanged = true
        configurationModel.onConfigurationChanged()
    }

    protected val rootPanel: JPanel = JPanel(BorderLayout())
    protected val splitPane: SplitPane = factory.createSplitPane()

    protected var currentDetailPanelType: CurrentDetailPanelType? = null
    protected var currentDetailPanel: ContentPanel? = null
    protected var currentDetailPayload: Any? = null

    protected lateinit var applyListener: (DebuggerConfiguration) -> Unit
    protected lateinit var applyButton: JComponent
    protected lateinit var leftComponent: JComponent

    override fun init(applyListener: (DebuggerConfiguration) -> Unit) {
        this.applyListener = applyListener
        contentPane = rootPanel

        isModal = true

        createConfigurationModel()
        configurationTree = initConfigurationTree()
        initTreeListener()

        leftComponent = JPanel(BorderLayout()).apply { minimumSize = Dimension(180, minimumSize.height) }
        leftComponent.add(factory.createScrollPane().apply { setViewportView(configurationTree) }, BorderLayout.CENTER)

        splitPane.left = leftComponent
        splitPane.right = JPanel()

        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)

        createActions()
        createButtons()
        createToolbarListener()

        setSize(700, 400)
        if (parent != null)
            setLocationRelativeTo(parent)

        configurationModel.onConfigurationChanged()
        isChanged = false

        if (changingConfiguration.responseIntercept.isEmpty()) {
            MainThreadDispatcher.dispatch {
                changingConfiguration.addResponseIntercept(".*bootstrap", "GET", true)
            }
        }
    }

    override fun focusOnNode(node: ConfigurationNode<*>) {
        configurationTree.selectionPath = (node.treeNode as javax.swing.tree.TreeNode).path()
    }

    protected open fun createActions() {
        val debugToolbar = SwingDebugToolbar()
        this.debugToolbar = debugToolbar
        leftComponent.add(debugToolbar, BorderLayout.NORTH)
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
            val renderer = CheckBoxNodeRenderer()
            cellRenderer = renderer

            val editor = CheckBoxNodeEditor(this)
            cellEditor = editor
            isEditable = true
            isRootVisible = false

            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        }
    }

    protected open fun initTreeListener() {
        configurationModel.tree = configurationTree
        configurationTree.addTreeSelectionListener { _ ->
            val component = (configurationTree.lastSelectedPathComponent as? TreeNode)?.configurationNode
            when (component) {
                is DelaysConfigurationRootNode -> onDelaySelected()
                is BlacklistRootNode, is RequestOverrideRootNode -> onRootNodeSelected()
                is BlacklistItemNode -> onBlacklistNodeSelected(component.nodeData!!)
                is RequestOverrideNode -> onRequestOverrideSelected(component.requestOverride)
            }
        }
    }

    protected open fun onDelaySelected() {
        if (currentDetailPanelType == CurrentDetailPanelType.DELAYS)
            return

        currentDetailPanel?.applyToModel()
        currentDetailPanelType = CurrentDetailPanelType.DELAYS
        val right = DelaysConfigurationPanel(changingConfiguration) {
            isChanged = true
        }
        currentDetailPanel = right
        splitPane.right = JScrollPane(right)
    }

    protected open fun onRootNodeSelected() {
        currentDetailPanel?.applyToModel()
        currentDetailPanel = null
        currentDetailPanelType = null
        splitPane.right = JPanel()
        return
    }

    protected open fun onBlacklistNodeSelected(regex: String) {
        if (currentDetailPanelType == CurrentDetailPanelType.BLACKLIST && currentDetailPayload == regex)
            return

        currentDetailPanel?.applyToModel()

        currentDetailPanelType = CurrentDetailPanelType.BLACKLIST
        currentDetailPayload = regex

        val right = BlacklistPanel(changingConfiguration)
        currentDetailPanel = right
        splitPane.right = JScrollPane(right.apply {
            val item = changingConfiguration.blacklistConfiguration.first { it.item == regex }
            init(item.item, item.enabled)
        })
    }

    protected open fun onRequestOverrideSelected(request: LocalRequestOverride) {
        if (currentDetailPanelType == CurrentDetailPanelType.REQUEST_OVERRIDE && currentDetailPayload == request)
            return

        currentDetailPanel?.applyToModel()

        currentDetailPanelType = CurrentDetailPanelType.REQUEST_OVERRIDE
        currentDetailPayload = request

        val right = RequestOverridePanel(changingConfiguration) {
            isChanged = true
        }
        currentDetailPanel = right
        splitPane.right = JScrollPane(right.apply {
            val item = changingConfiguration.requestOverride.first { it.item.id == request.id }
            init(item.item, item.enabled)
        })
    }

    protected open fun onCancel() {
        visibility = false
    }

    protected open fun onApply() {
        currentDetailPanel?.applyToModel()
        isChanged = false
        applyListener(changingConfiguration)
    }

    protected open fun updatePanelCheckedStateIfRequired(node: CheckedNode) {
        val configurationNode = node.configurationNode
        when (configurationNode) {
            is DelaysConfigurationRootNode -> if (currentDetailPanelType == CurrentDetailPanelType.DELAYS) {
                currentDetailPanel?.updateEnabledFlag(node.nodeCheckState)
            }
            is BlacklistRootNode -> {
                val enabled = node.nodeCheckState
                changingConfiguration.blacklistConfiguration.forEach { it.enabled = enabled }
                configurationModel.onConfigurationChanged()
            }
            is BlacklistItemNode ->
                if (currentDetailPanelType == CurrentDetailPanelType.BLACKLIST
                        && configurationNode.regex == currentDetailPayload) {
                    currentDetailPanel?.updateEnabledFlag(node.nodeCheckState)
                }
        }
    }

    protected open fun updateConfigurationModel(treeNode: TreeNode) {
        configurationModel.nodeChanged(treeNode)
    }

    protected open fun createConfigurationModel() {
        configurationModel = ConfigurationModel(changingConfiguration, SwingNodeBuilder {
            updateConfigurationModel(it)
            updatePanelCheckedStateIfRequired(it)
            isChanged = true
        })
    }

    protected open fun createToolbarListener() {
        debugToolbar.listener = NiddlerDebugConfigurationHelper(this,
                factory, this, changingConfiguration, configurationModel)
    }

    override fun removeCurrentItem() {
        val component = (configurationTree.lastSelectedPathComponent as? TreeNode)?.configurationNode
        when (component) {
            is BlacklistItemNode -> {
                changingConfiguration.removeBlacklistItem(component.regex)
                currentDetailPanel = null
                currentDetailPanelType = null
                splitPane.right = JPanel()
                configurationTree.clearSelection()
            }
        }
    }

    enum class CurrentDetailPanelType {
        DELAYS,
        BLACKLIST,
        REQUEST_OVERRIDE
    }

}