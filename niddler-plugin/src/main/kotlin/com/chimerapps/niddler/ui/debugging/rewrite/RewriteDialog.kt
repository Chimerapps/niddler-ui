package com.chimerapps.niddler.ui.debugging.rewrite

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteSet
import com.intellij.openapi.project.Project
import com.intellij.ui.table.JBTable
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableModel

class RewriteDialog(parent: Window?, private val project: Project?) : JDialog(parent, "Rewrite settings", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, project: Project?) {
            val dialog = RewriteDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            //TODO
        }
    }

    private val rootContainer = JPanel(GridBagLayout())
    private val rules = mutableListOf<RewriteSet>()

    private val masterPanel = RewriteMasterPanel(project, ::onRewriteSetAdded, ::onRewriteSetRemoved, ::onMasterSelectionChanged).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.NORTHWEST
            weightx = 35.0
            weighty = 100.0
        }
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 10, 10) }, constraints)
    }
    private val detailPanel = RewriteDetailPanel(this, ::onItemUpdated).also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.NORTHEAST
            weightx = 65.0
            weighty = 100.0
        }
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 10, 10) }, constraints)
    }

    init {
        val screenSize = Toolkit.getDefaultToolkit().screenSize

        contentPane = rootContainer
        minimumSize = Dimension(screenSize.width / 3, screenSize.height / 3)
        pack()
        size = minimumSize
    }

    private fun onRewriteSetRemoved(index: Int){
        rules.removeAt(index)
    }

    private fun onRewriteSetAdded(rule: RewriteSet) {
        rules.add(rule)
    }

    private fun onMasterSelectionChanged(selectedItem: RewriteSet?) {
        detailPanel.currentItem = selectedItem
    }

    private fun onItemUpdated(old: RewriteSet, new: RewriteSet) {
        val oldIndex = rules.indexOf(old)
        rules[oldIndex] = new
        masterPanel.rewriteSetUpdated(oldIndex, new)
    }
}
