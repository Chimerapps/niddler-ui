package com.icapps.niddler.ui.form.debug.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.debug.ConfigurationModel
import com.icapps.niddler.ui.form.debug.DebugToolbar
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.content.BlacklistPanel
import com.icapps.niddler.ui.form.debug.nodes.renderer.CheckboxCellEditor
import com.icapps.niddler.ui.form.debug.nodes.renderer.CheckedCellRenderer
import com.icapps.niddler.ui.form.debug.nodes.renderer.DefaultCellRenderer
import java.awt.BorderLayout
import java.awt.Window
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTree

/**
 * @author nicolaverbeeck
 */
open class SwingNiddlerDebugConfigurationDialog(parent: Window?, private val factory: ComponentsFactory)
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

    override fun init() {
        contentPane = rootPanel

        isModal = true

        configurationModel = ConfigurationModel()
        configurationTree = initConfigurationTree()

        splitPane.left = factory.createScrollPane().apply { setViewportView(configurationTree) }
        splitPane.right = BlacklistPanel()

        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)

        createActions()

        setSize(400, 200)
        if (parent != null)
            setLocationRelativeTo(parent)
    }

    protected fun createActions() {
        val debugToolbar = SwingDebugToolbar()
        rootPanel.add(debugToolbar, BorderLayout.NORTH)
        this.debugToolbar = debugToolbar
    }

    protected fun initConfigurationTree(): JTree {
        return JTree(configurationModel.treeModel).apply {

            showsRootHandles = true
            cellRenderer = CheckedCellRenderer(DefaultCellRenderer())
            cellEditor = CheckboxCellEditor(cellRenderer as CheckedCellRenderer, this)
            isEditable = true
            isRootVisible = false
        }
    }

}