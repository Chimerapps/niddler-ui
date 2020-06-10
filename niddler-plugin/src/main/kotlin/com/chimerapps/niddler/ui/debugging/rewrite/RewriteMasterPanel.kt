package com.chimerapps.niddler.ui.debugging.rewrite

import com.chimerapps.niddler.ui.util.ui.CheckBox
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
import com.chimerapps.niddler.ui.util.ui.chooseOpenFile
import com.chimerapps.niddler.ui.util.ui.chooseSaveFile
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteExporter
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteImporter
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteSet
import com.intellij.openapi.project.Project
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBScrollPane
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.util.UUID
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class RewriteMasterPanel(private val project: Project?,
                         private val onRewriteSetAdded: (RewriteSet) -> Unit,
                         private val onRewriteSetRemoved: (Int) -> Unit,
                         private val selectionListener: (RewriteSet?) -> Unit) : JPanel(GridBagLayout()) {

    var allEnabled: Boolean = false
        set(value) {
            field = value
            rulesList.isEnabled = value
            enableAllToggle.isSelected = value

            val selectedIndices = rulesList.selectedIndices
            val enableExportAndRemove = value && selectedIndices.isNotEmpty()
            exportButton.isEnabled = enableExportAndRemove
            removeButton.isEnabled = enableExportAndRemove

            if (value && selectedIndices.size == 1) {
                selectionListener(rulesList.getItemAt(selectedIndices[0]))
            } else {
                selectionListener(null)
            }
        }

    @Suppress("MoveLambdaOutsideParentheses")
    val enableAllToggle = CheckBox("Enable rewrite", ::allEnabled).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
            gridheight = 1
            anchor = GridBagConstraints.NORTHWEST
            weightx = 100.0
        }
        add(it, constraints)
    }

    private val rulesList = CheckBoxList<RewriteSet>().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.CENTER
            weightx = 100.0
            weighty = 100.0
        }
        it.isEnabled = allEnabled
        it.selectionModel.addListSelectionListener { _ ->
            val selectedIndices = it.selectedIndices
            val hasSelection = selectedIndices.isNotEmpty()
            exportButton.isEnabled = hasSelection && allEnabled
            removeButton.isEnabled = hasSelection && allEnabled

            if (allEnabled) {
                if (selectedIndices.size == 1) {
                    selectionListener(it.getItemAt(selectedIndices[0]))
                } else {
                    selectionListener(null)
                }
            } else {
                selectionListener(null)
            }

        }
        it.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        add(JBScrollPane(it).also { scroller -> scroller.border = BorderFactory.createLineBorder(Color.GRAY) }, constraints)
    }

    private val addButton = JButton("Add").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            val ruleSet = RewriteSet(true, "Unnamed", emptyList(), emptyList(), UUID.randomUUID().toString())
            onRewriteSetAdded(ruleSet)
            rulesList.addItem(ruleSet, ruleSet.name, ruleSet.active)
            rulesList.selectedIndex = rulesList.itemsCount - 1
        }
    }

    private val removeButton: JButton = JButton("Remove").also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.EAST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            rulesList.selectedIndices.reversed().forEach { index ->
                onRewriteSetRemoved(index)
                (rulesList.model as DefaultListModel).remove(index)
                print("Items left: ${rulesList.model.size}")
            }
        }
    }

    private val importButton = JButton("Import").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            showImportDialog()
        }
    }

    private val exportButton = JButton("Export").also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 3
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.EAST
            weightx = 50.0
        }
        add(it, constraints)
        it.addActionListener {
            showExportDialog()
        }
    }

    init {
        val enableExportAndRemove = allEnabled && rulesList.selectedIndices.isNotEmpty()
        exportButton.isEnabled = enableExportAndRemove
        removeButton.isEnabled = enableExportAndRemove
    }

    private fun showImportDialog() {
        val file = chooseOpenFile("Select file") ?: return
        try {
            val importedData = file.inputStream.use { RewriteImporter().import(it) }

            importedData.forEach { ruleSet ->
                onRewriteSetAdded(ruleSet)
                rulesList.addItem(ruleSet, ruleSet.name, ruleSet.active)
            }
        } catch (e: Throwable) {
            NotificationUtil.error("Failed to import", e.message ?: "Failed to parse file", project)
        }
    }

    private fun showExportDialog() {
        val items = rulesList.selectedIndices.map { rulesList.getItemAt(it) }.filterNotNull()
        if (items.isEmpty()) return

        var file = chooseSaveFile("Export to", ".xml") ?: return
        if (file.extension.isEmpty()) {
            file = File(file.absolutePath + ".xml")
        }
        file.outputStream().use { RewriteExporter().export(items, it) }
        NotificationUtil.info("Rule export complete", "<html>Rule export completed to <a href=\"file://${file.absolutePath}\">${file.name}</a></html>", project)
    }

    fun rewriteSetUpdated(index: Int, new: RewriteSet) {
        rulesList.updateItem(rulesList.getItemAt(index)!!, new, new.name)
    }

    fun addRewriteSets(sets: List<RewriteSet>, selectLast: Boolean) {
        sets.forEach { ruleSet ->
            onRewriteSetAdded(ruleSet)
            rulesList.addItem(ruleSet, ruleSet.name, ruleSet.active)
        }
        if (selectLast)
            rulesList.selectedIndex = rulesList.itemsCount - 1
    }

    fun isRewriteSetEnabled(set: RewriteSet): Boolean {
        for (i in 0 until rulesList.itemsCount) {
            if (rulesList.getItemAt(i)?.id == set.id)
                return rulesList.isItemSelected(i)
        }
        return false
    }

}