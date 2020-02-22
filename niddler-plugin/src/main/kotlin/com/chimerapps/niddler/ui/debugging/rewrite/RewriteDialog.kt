package com.chimerapps.niddler.ui.debugging.rewrite

import com.chimerapps.niddler.ui.util.ui.CheckBox
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBScrollPane
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.Window
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

class RewriteDialog(parent: Window?) : JDialog(parent, "Rewrite settings", ModalityType.APPLICATION_MODAL) {

    private val rootContainer = JPanel(GridBagLayout())

    private val masterPanel = RewriteDetailPanel().also {
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

private class RewriteDetailPanel : JPanel(GridBagLayout()) {

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

    val rulesList = CheckBoxList<RewriteRuleHolder>().also {
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
    }

    init {
        rulesList.addItem(RewriteRuleHolder(false, "Testing123"), "Testing123", false)
        rulesList.addItem(RewriteRuleHolder(true, "Testing1234"), "Testing123", true)
        rulesList.addItem(RewriteRuleHolder(true, "Testing12345"), "Testing1235", true)
    }

}

private class RewriteMasterPanel : JPanel() {
    init {
        background = Color.BLUE
    }
}

private class RewriteRuleHolder(val enabled: Boolean, val name: String) {

}