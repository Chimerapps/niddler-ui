package com.chimerapps.niddler.ui.debugging.rewrite.location

import com.chimerapps.niddler.ui.util.ui.NumberOrRegexDocumentFilter
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.text.AbstractDocument

@Suppress("DuplicatedCode")
open class EditLocationDialogUI(addButtons: Boolean) {

    val content = object : JPanel(GridBagLayout()) {
        override fun getPreferredSize(): Dimension {
            return Dimension(parent.width, super.getPreferredSize().height)
        }
    }.also {
        it.border = BorderFactory.createEmptyBorder(20, 20, 0, 20)
    }

    val protocolChooser = ComboBox<String>().also {
        it.isEditable = true
        it.addItem("")
        it.addItem("http")
        it.addItem("https")

        addLabel("Protocol:", 1)

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 1
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(it, constraints)
    }

    val host = JBTextField().also {
        addLabel("Host:", 2)

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 2
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
        content.add(it, constraints)
    }

    val port = JBTextField(5).also {
        addLabel("Port:", 3)

        (it.document as AbstractDocument).documentFilter = NumberOrRegexDocumentFilter()

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 3
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(it, constraints)
    }

    val path = JBTextField().also {
        addLabel("Path:", 4)

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 4
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
        content.add(it, constraints)
    }

    val query = JBTextField().also {
        addLabel("Query:", 5)

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 5
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
        content.add(it, constraints)
    }

    init {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 6
            gridwidth = 2
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(JBLabel("Empty fields match all values. Wildcards * and ? may be used.").also {
            it.font = it.font.deriveFont(10.0f)
        }.also { it.border = BorderFactory.createEmptyBorder(5, 0, 0, 0) }, constraints)
    }

    val buttonPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 7
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.EAST
        }
        it.border = BorderFactory.createEmptyBorder(10, 0, 10, 0)
        if (addButtons)
            content.add(it, constraints)
    }

    val cancelButton = JButton("Cancel").also {
        buttonPanel.add(it)
    }

    val okButton = JButton("OK").also {
        buttonPanel.add(it)
    }

    private fun addLabel(label: String, row: Int) {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = row
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.EAST
        }
        content.add(JBLabel(label, SwingConstants.RIGHT).also {
            it.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
        }, constraints)
    }
}