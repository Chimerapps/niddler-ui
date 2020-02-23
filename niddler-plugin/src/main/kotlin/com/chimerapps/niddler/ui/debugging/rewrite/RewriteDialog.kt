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
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.Window
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class RewriteDialog(parent: Window?, private val project: Project?) : JDialog(parent, "Rewrite settings", ModalityType.APPLICATION_MODAL) {

    private val rootContainer = JPanel(GridBagLayout())

    private val masterPanel = RewriteDetailPanel(project).also {
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
    private val detailPanel = RewriteMasterPanel().also {
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
        rootContainer.add(it, constraints)
    }

    init {
        val screenSize = Toolkit.getDefaultToolkit().screenSize

        contentPane = rootContainer
        minimumSize = Dimension(screenSize.width / 3, screenSize.height / 3)
        pack()
    }
}

private class RewriteDetailPanel(private val project: Project?) : JPanel(GridBagLayout()) {

    var allEnabled: Boolean = false
        set(value) {
            field = value
            rulesList.isEnabled = value
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

    val rulesList = CheckBoxList<RewriteSet>().also {
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
        it.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        add(JBScrollPane(it).also { scroller -> scroller.border = BorderFactory.createLineBorder(Color.GRAY) }, constraints)
    }

    val addButton = JButton("Add").also {
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
    }

    val removeButton = JButton("Remove").also {
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
    }

    val importButton = JButton("Import").also {
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

    private fun showImportDialog() {
        val file = chooseOpenFile("Select file") ?: return
        try {
            val importedData = file.inputStream.use { RewriteImporter().import(it) }

            importedData.forEach { ruleSet ->
                rulesList.addItem(ruleSet, ruleSet.name, ruleSet.active)
            }
        } catch (e: Throwable) {
            NotificationUtil.error("Failed to import", e.message ?: "Failed to parse file", project)
        }
    }

    val exportButton = JButton("Export").also {
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

}

private class RewriteMasterPanel : JPanel() {
    init {
        background = Color.BLUE
    }
}