package com.chimerapps.niddler.ui.debugging.rewrite.location

import com.chimerapps.niddler.ui.util.localization.Tr
import com.chimerapps.niddler.ui.util.ui.NumberOrRegexDocumentFilter
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.text.AbstractDocument

open class EditLocationUI(includeAction: Boolean, includeButtons: Boolean) {

    val content = JPanel(GridBagLayout()).also {
        it.border = BorderFactory.createEmptyBorder(20, 20, 0, 20)
    }

    private var gridY = 1

    val actionChooser: ComboBox<String>? = if (includeAction) ComboBox<String>().also {
        it.isEditable = true
        it.addItem("")
        it.addItem("GET")
        it.addItem("PUT")
        it.addItem("POST")
        it.addItem("DELETE")
        it.addItem("HEAD")
        it.addItem("PATCH")
        it.addItem("OPTIONS")

        addLabel("Action:", gridY)

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = gridY++
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(it, constraints)
    } else null

    val protocolChooser = ComboBox<String>().also {
        it.isEditable = true
        it.addItem("")
        it.addItem("http")
        it.addItem("https")

        addLabel(Tr.EditLocationProtocol.tr(), gridY)

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = gridY++
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(it, constraints)
    }

    val host = JBTextField().also {
        addLabel(Tr.EditLocationHost.tr(), gridY)

        val constraints = createGenericValueConstraints()
        content.add(it, constraints)
    }

    val port = JBTextField(5).also {
        addLabel(Tr.EditLocationPort.tr(), gridY)

        (it.document as AbstractDocument).documentFilter = NumberOrRegexDocumentFilter()

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = gridY++
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
            weightx = 20.0
        }
        content.add(it, constraints)
    }

    val path = JBTextField().also {
        addLabel(Tr.EditLocationPath.tr(), gridY)

        val constraints = createGenericValueConstraints()
        content.add(it, constraints)
    }

    val query = JBTextField().also {
        addLabel(Tr.EditLocationQuery.tr(), gridY)

        val constraints = createGenericValueConstraints()
        content.add(it, constraints)
    }

    init {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = gridY++
            gridwidth = 2
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(JBLabel(Tr.EditLocationMatchDescription.tr()).also {
            it.font = it.font.deriveFont(10.0f)
        }.also { it.border = BorderFactory.createEmptyBorder(5, 0, 0, 0) }, constraints)
    }

    private val buttonPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = gridY++
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.EAST
        }
        it.border = BorderFactory.createEmptyBorder(10, 0, 10, 0)
        if (includeButtons)
            content.add(it, constraints)
    }

    val cancelButton = JButton(Tr.EditLocationDialogCancel.tr()).also {
        buttonPanel.add(it)
    }

    val okButton = JButton(Tr.EditLocationDialogOk.tr()).also {
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

    private fun createGenericValueConstraints(): GridBagConstraints {
        return GridBagConstraints().apply {
            gridx = 1
            gridy = gridY++
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 100.0
        }
    }
}