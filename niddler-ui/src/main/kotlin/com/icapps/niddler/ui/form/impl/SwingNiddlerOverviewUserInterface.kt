package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.form.PopupMenuSelectingJTable
import com.icapps.niddler.ui.form.ui.NiddlerOverviewUserInterface
import com.icapps.niddler.ui.model.ui.LinkedMessagesRenderer
import javax.swing.JTable
import javax.swing.JTree

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
open class SwingNiddlerOverviewUserInterface(private val parent: NiddlerOverviewParent) : NiddlerOverviewUserInterface {

    override lateinit var messagesAsTable: PopupMenuSelectingJTable
    override lateinit var messagesAsTree: JTree

    override fun init() {
        initMessagesAsTable()
        initMessagesAsTree()
    }

    protected open fun initMessagesAsTable() {
        messagesAsTable = PopupMenuSelectingJTable().apply {
            fillsViewportHeight = false
            rowHeight = 32
            showHorizontalLines = true
            showVerticalLines = false
        }
    }

    protected open fun initMessagesAsTree() {
        messagesAsTree = JTree().apply {
            rowHeight = 32
            isEditable = false
            dragEnabled = false
            isRootVisible = false
            showsRootHandles = true
            cellRenderer = LinkedMessagesRenderer(0)
        }
    }

    override fun showTable() {
        parent.showTable()
    }

    override fun showLinked() {
        parent.showLinked()
    }

    override fun showDebugView() {
        parent.showDebugView()
    }

    interface NiddlerOverviewParent {
        fun showTable()
        fun showLinked()
        fun showDebugView()
    }
}
