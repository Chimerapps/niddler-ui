package com.chimerapps.niddler.ui.debugging.rewrite

import com.chimerapps.niddler.ui.model.ProjectConfig
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocation
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocationMatch
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteSet
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.intellij.openapi.project.Project
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.Window
import java.net.URI
import java.util.UUID
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

class RewriteDialog(parent: Window?, project: Project) : JDialog(parent, "Rewrite settings", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, project: Project): RewriteConfig? {
            val dialog = RewriteDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.response
        }

        fun showAdd(parent: Window?, project: Project, message: NiddlerMessageInfo): RewriteConfig? {
            val dialog = RewriteDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.addNewRuleFor(message)

            dialog.isVisible = true
            return dialog.response
        }
    }

    var response: RewriteConfig? = null
        private set

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
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 5, 10) }, constraints)
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
        rootContainer.add(it.also { it.border = BorderFactory.createEmptyBorder(10, 10, 5, 10) }, constraints)
    }
    private val buttonPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            gridheight = 1
            anchor = GridBagConstraints.SOUTHEAST
            weightx = 100.0
        }
        it.layout = BoxLayout(it, BoxLayout.X_AXIS)
        it.border = BorderFactory.createEmptyBorder(0, 0, 10, 10)
        rootContainer.add(it, constraints)
        it.add(Box.createGlue())
    }
    private val cancelButton = JButton("Cancel").also {
        buttonPanel.add(it)
        it.addActionListener {
            dispose()
        }
    }
    private val okButton = JButton("OK").also {
        buttonPanel.add(it)
        it.addActionListener {
            for (i in 0 until rules.size) {
                rules[i] = rules[i].copy(active = masterPanel.isRewriteSetEnabled(rules[i]))
            }
            response = RewriteConfig(masterPanel.allEnabled, rules)
            dispose()
        }
    }

    init {
        contentPane = rootContainer
        rootPane.defaultButton = okButton

        val screenSize = Toolkit.getDefaultToolkit().screenSize

        val minSize = Dimension(screenSize.width / 3, screenSize.height / 3)
        minimumSize = minSize
        size = minSize

        ProjectConfig.load<RewriteConfig>(project, ProjectConfig.CONFIG_REWRITE)?.let {
            val config = it.copy(sets = it.sets.createIds())
            masterPanel.allEnabled = config.allEnabled
            masterPanel.addRewriteSets(config.sets, selectLast = false)
        }
    }

    private fun onRewriteSetRemoved(index: Int) {
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
        if (oldIndex < 0) return
        rules[oldIndex] = new
        masterPanel.rewriteSetUpdated(oldIndex, new)
    }

    private fun addNewRuleFor(message: NiddlerMessageInfo) {
        val set = RewriteSet(active = true,
                name = "No name",
                locations = listOf(RewriteLocationMatch(enabled = true, location = createRewriteLocationFor(message))),
                rules = listOf(),
                id = UUID.randomUUID().toString())

        masterPanel.addRewriteSets(listOf(set), selectLast = true)
    }
}

private fun List<RewriteSet>.createIds(): List<RewriteSet> {
    return map { it.copy(id = UUID.randomUUID().toString()) }
}

data class RewriteConfig(val allEnabled: Boolean, val sets: List<RewriteSet>)

private fun createRewriteLocationFor(message: NiddlerMessageInfo): RewriteLocation {
    val uri = message.url?.let { URI.create(it) } ?: return RewriteLocation()

    return RewriteLocation(protocol = uri.scheme,
            port = uri.port.let { if (it == -1) null else it }?.toString(),
            query = uri.query,
            path = uri.path,
            host = uri.host)
}