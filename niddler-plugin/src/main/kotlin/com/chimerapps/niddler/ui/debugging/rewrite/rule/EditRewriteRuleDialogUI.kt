package com.chimerapps.niddler.ui.debugging.rewrite.rule

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.CheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import java.awt.Dialog
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Window
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.border.Border

@Suppress("DuplicatedCode")
open class EditRewriteRuleDialogUI(parent: Window?) : JDialog(parent, "Edit Rewrite Rule", Dialog.ModalityType.APPLICATION_MODAL) {

    private companion object {
        private fun createPanelBorder(title: String): Border {
            return BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title)
        }
    }

    protected val content = JPanel(GridBagLayout()).also {
        it.border = BorderFactory.createEmptyBorder(20, 20, 0, 20)
    }

    protected val typeChooser = ComboBox<RewriteType?>().also {
        it.addItem(null)
        RewriteType.values().forEach(it::addItem)

        @Suppress("UNCHECKED_CAST")
        val defaultRenderer = it.renderer as ListCellRenderer<Any>
        it.renderer = ListCellRenderer { list, value, index, isSelected, cellHasFocus ->
            val label = when (value) {
                null -> ""
                RewriteType.ADD_HEADER -> "Add header"
                RewriteType.MODIFY_HEADER -> "Modify header"
                RewriteType.REMOVE_HEADER -> "Remove header"
                RewriteType.HOST -> "Host"
                RewriteType.PATH -> "Path"
                RewriteType.URL -> "Url"
                RewriteType.ADD_QUERY_PARAM -> "Add query parameter"
                RewriteType.MODIFY_QUERY_PARAM -> "Modify query parameter"
                RewriteType.REMOVE_QUERY_PARAM -> "Remove query parameter"
                RewriteType.RESPONSE_STATUS -> "Response status"
                RewriteType.BODY -> "Body"
            }
            defaultRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus)
        }

        val labelConstraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(JBLabel("Type:").also { label ->
            label.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
        }, labelConstraints)

        val pickerConstraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
        content.add(it, pickerConstraints)
    }

    private val wherePanel = JPanel(GridBagLayout()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        it.border = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 10, 0),
                createPanelBorder("Where"))

        content.add(it, constraints)
    }

    protected val requestCheckbox = CheckBox("Request").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
            weightx = 50.0
        }
        wherePanel.add(it, constraints)
    }

    protected val responseCheckbox = CheckBox("Response").also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
            weightx = 50.0
        }
        wherePanel.add(it, constraints)
    }

    private val matchPanel = JPanel(GridBagLayout()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        it.border = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 10, 0),
                BorderFactory.createCompoundBorder(
                        createPanelBorder("Match"),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ))

        content.add(it, constraints)
    }

    private val matchLabel = JBLabel("Enter text to match or leave blank to match all").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 4
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        matchPanel.add(it, constraints)
    }

    private val nameLabel = JBLabel("Name:").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }

        matchPanel.add(it, constraints)
    }
    protected val matchNameText = JBTextField().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 1
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        matchPanel.add(it, constraints)
    }
    protected val matchNameRegex = CheckBox("Regex").also {
        val constraints = GridBagConstraints().apply {
            gridx = 3
            gridy = 1
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }

        matchPanel.add(it, constraints)
    }
    private val valueLabel = JBLabel("Value:").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }

        matchPanel.add(it, constraints)
    }
    protected val matchValueText = JBTextField().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 2
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        matchPanel.add(it, constraints)
    }
    protected val matchValueRegex = CheckBox("Regex").also {
        val constraints = GridBagConstraints().apply {
            gridx = 3
            gridy = 2
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }

        matchPanel.add(it, constraints)
    }
    protected val matchEntire = CheckBox("Match whole value").also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 3
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        matchPanel.add(it, constraints)
    }
    protected val caseSensitive = CheckBox("Case sensitive").also {
        val constraints = GridBagConstraints().apply {
            gridx = 2
            gridy = 3
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        matchPanel.add(it, constraints)
    }
    private val replacePanel = JPanel(GridBagLayout()).also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        it.border = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 10, 0),
                BorderFactory.createCompoundBorder(
                        createPanelBorder("Replace"),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ))

        content.add(it, constraints)
    }
    private val replaceNameLabel = JBLabel("Name:").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }

        replacePanel.add(it, constraints)
    }
    protected val replaceNameText = JBTextField().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        replacePanel.add(it, constraints)
    }
    private val replaceValueLabel = JBLabel("Value:").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }

        replacePanel.add(it, constraints)
    }
    protected val replaceValueText = JBTextField().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 1
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        replacePanel.add(it, constraints)
    }
    protected val replaceFirst = JBRadioButton("Replace first").also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        replacePanel.add(it, constraints)
    }
    protected val replaceAll = JBRadioButton("Replace all").also {
        val constraints = GridBagConstraints().apply {
            gridx = 2
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }

        replacePanel.add(it, constraints)
    }
    private val replaceLabel = JBLabel("<html>Enter new values or leave blank for no change. If using regex matches you may enter references to groups, eg. $1</html>").also {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 3
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.WEST
            weightx = 100.0
        }
        it.border = BorderFactory.createEmptyBorder(4, 0, 0, 0)

        replacePanel.add(it, constraints)
    }

    private val buttonPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 4
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.EAST
        }
        it.border = BorderFactory.createEmptyBorder(10, 0, 10, 0)
        content.add(it, constraints)
    }

    protected val cancelButton = JButton("Cancel").also {
        buttonPanel.add(it)
    }

    protected val okButton = JButton("OK").also {
        buttonPanel.add(it)
    }

    init {
        contentPane = content
        rootPane.defaultButton = okButton

        ButtonGroup().also {
            it.add(replaceAll)
            it.add(replaceFirst)
        }
    }

}