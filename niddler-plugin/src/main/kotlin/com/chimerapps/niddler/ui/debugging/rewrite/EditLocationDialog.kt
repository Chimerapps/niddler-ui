package com.chimerapps.niddler.ui.debugging.rewrite

import com.chimerapps.niddler.ui.util.ui.NumberOnlyDocumentFilter
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocation
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.SwingConstants
import javax.swing.text.AbstractDocument


class EditLocationDialog(parent: Window?, source: RewriteLocation?) : JDialog(parent, "Edit Location", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, source: RewriteLocation?): RewriteLocation? {
            val dialog = EditLocationDialog(parent, source)
            dialog.pack()
            dialog.setSize(420, dialog.height)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.result
        }
    }

    private val content = JPanel(GridBagLayout()).also {
        it.border = BorderFactory.createEmptyBorder(20, 20, 0, 20)
    }

    var result: RewriteLocation? = null
        private set

    private val protocolChooser = ComboBox<String>().also {
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

    private val host = JTextField().also {
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

    private val port = JTextField(5).also {
        addLabel("Port:", 3)

        (it.document as AbstractDocument).documentFilter = NumberOnlyDocumentFilter()

        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 3
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.WEST
        }
        content.add(it, constraints)
    }

    private val path = JTextField().also {
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

    private val query = JTextField().also {
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

        contentPane = content

        content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        protocolChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    }

    private val buttonPanel = JPanel().also {
        val constraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 7
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.EAST
        }
        it.border = BorderFactory.createEmptyBorder(10, 0, 10, 0)
        content.add(it, constraints)
    }

    private val cancelButton = JButton("Cancel").also {
        buttonPanel.add(it)
        it.addActionListener { dispose() }
    }

    private val okButton = JButton("OK").also {
        buttonPanel.add(it)
        it.addActionListener {
            result = makeResult()
            dispose()
        }
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

    init {
        rootPane.defaultButton = okButton

        source?.let { initFrom ->
            initFrom.protocol?.let { protocolChooser.selectedItem = it }
            initFrom.host?.let { host.text = it }
            initFrom.port?.let { port.text = it.toString() }
            initFrom.path?.let { path.text = it }
            initFrom.query?.let { query.text = it }
        }
    }

    private fun makeResult(): RewriteLocation {
        return RewriteLocation(protocol = (protocolChooser.selectedItem as String).trimToNull(),
                host = host.text.trimToNull(),
                path = path.text.trimToNull(),
                query = query.text.trimToNull(),
                port = port.text.trimToNull()?.toInt())
    }
}

private fun String.trimToNull(): String? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null
    return trimmed
}