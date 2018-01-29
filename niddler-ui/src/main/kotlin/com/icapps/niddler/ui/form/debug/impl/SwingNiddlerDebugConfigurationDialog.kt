package com.icapps.niddler.ui.form.debug.impl

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.debug.ConfigurationModel
import com.icapps.niddler.ui.form.debug.DebugToolbar
import com.icapps.niddler.ui.form.debug.NiddlerDebugConfigurationDialog
import com.icapps.niddler.ui.form.debug.nodes.renderer.CheckboxCellEditor
import com.icapps.niddler.ui.form.debug.nodes.renderer.CheckedCellRenderer
import com.icapps.niddler.ui.form.debug.nodes.renderer.DefaultCellRenderer
import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.JWindow

/**
 * @author nicolaverbeeck
 */
open class SwingNiddlerDebugConfigurationDialog(parent: JWindow?, private val factory: ComponentsFactory)
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

        configurationModel = ConfigurationModel()
        configurationTree = initConfigurationTree()

        splitPane.left = factory.createScrollPane().apply { setViewportView(configurationTree) }

        rootPanel.add(splitPane.asComponent, BorderLayout.CENTER)

        setSize(400, 200)
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