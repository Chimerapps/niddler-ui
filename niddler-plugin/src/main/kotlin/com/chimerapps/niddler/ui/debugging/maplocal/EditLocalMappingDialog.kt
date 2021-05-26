package com.chimerapps.niddler.ui.debugging.maplocal

import com.chimerapps.niddler.ui.debugging.rewrite.location.EditLocationDialogUI
import com.chimerapps.niddler.ui.util.ext.trimToNull
import com.icapps.niddler.lib.debugger.model.maplocal.MapLocalEntry
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocation
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.RenderingHints
import java.awt.Window
import java.awt.event.KeyEvent
import java.io.File
import java.util.UUID
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingConstants

/**
 * @author Nicola Verbeeck
 */
class EditLocalMappingDialog(parent: Window?, source: MapLocalEntry?, project: Project?) : JDialog(parent, "Edit Mapping", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, source: MapLocalEntry?, project: Project?): MapLocalEntry? {
            val dialog = EditLocalMappingDialog(parent, source, project)
            dialog.pack()
            dialog.setSize(420, dialog.height)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.result
        }
    }

    var result: MapLocalEntry? = null
        private set

    private val content = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
    }
    private val editLocationUI = EditLocationDialogUI(addButtons = false)
    private val browseButton: TextFieldWithBrowseButton

    init {
        content.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        editLocationUI.protocolChooser.registerKeyboardAction({ dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        rootPane.defaultButton = editLocationUI.okButton

        editLocationUI.okButton.addActionListener {
            result = makeResult()
            if (result != null) {
                dispose()
            }
        }
        editLocationUI.cancelButton.addActionListener {
            dispose()
        }

        source?.let { initFrom ->
            initFrom.location.protocol?.let { editLocationUI.protocolChooser.selectedItem = it }
            initFrom.location.host?.let { editLocationUI.host.text = it }
            initFrom.location.port?.let { editLocationUI.port.text = it.toString() }
            initFrom.location.path?.let { editLocationUI.path.text = it }
            initFrom.location.query?.let { editLocationUI.query.text = it }
        }

        val locationPanel = object : JPanel(BorderLayout()) {
            override fun getPreferredSize(): Dimension = Dimension(this.parent.width, super.getPreferredSize().height)
        }.also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        }
        locationPanel.border = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Map From")
        locationPanel.add(BackgroundRenderingJPanel(Color(0x11000000, true)).also {
            it.add(editLocationUI.content)
            editLocationUI.content.background = Color(0, true)
        }, BorderLayout.NORTH)

        content.add(locationPanel)

        val mapToPanel = object : JPanel(GridBagLayout()) {
            override fun getPreferredSize(): Dimension {
                return Dimension(this.parent.width, super.getPreferredSize().height)
            }
        }.also {
            it.border = BorderFactory.createEmptyBorder(20, 20, 0, 20)
        }

        mapToPanel.add(JBLabel("Local path:", SwingConstants.RIGHT).also {
            it.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
        }, GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.EAST
        })

        browseButton = TextFieldWithBrowseButton().also {
            mapToPanel.add(it, GridBagConstraints().apply {
                gridx = 1
                gridy = 1
                gridwidth = 1
                gridheight = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 100.0
            })
        }
        browseButton.addBrowseFolderListener(
            "Local File/Folder", "Local file or folder to map to",
            project,
            FileChooserDescriptor(true, true, false, false, false, false)
        )
        browseButton.text = source?.destination ?: ""

        val filePanel = object : JPanel() {
            override fun getPreferredSize(): Dimension = Dimension(this.parent.width, super.getPreferredSize().height)
        }.also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        }
        filePanel.border = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Map To")
        filePanel.add(BackgroundRenderingJPanel(Color(0x11000000, true)).also {
            it.add(mapToPanel)
            mapToPanel.background = Color(0, true)
        }, BorderLayout.NORTH)

        content.add(filePanel)

        content.add(object : JPanel() {
            override fun getPreferredSize(): Dimension = Dimension(this.parent.width, super.getPreferredSize().height)
        }.also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(JBTextArea("You can select a directory instead of a file to map remote paths to the local structure").also { child ->
                child.lineWrap = true
                child.background = Color(0, true)
                child.isEditable = false
            }, BorderLayout.WEST)
        })

        content.add(Box.createVerticalGlue())
        content.add(editLocationUI.buttonPanel)

        contentPane = content.padding(left = 10, right = 10, bottom = 10, top = 10)
    }

    private fun makeResult(): MapLocalEntry? {
        if (!File(browseButton.text).exists()) {
            JOptionPane.showMessageDialog(this, "Provided path does not exist")
            return null
        }
        return MapLocalEntry(
            enabled = true,
            location = RewriteLocation(
                protocol = (editLocationUI.protocolChooser.selectedItem as String).trimToNull(),
                host = editLocationUI.host.text.trimToNull(),
                path = editLocationUI.path.text.trimToNull(),
                query = editLocationUI.query.text.trimToNull(),
                port = editLocationUI.port.text.trimToNull()
            ),
            caseSensitive = true,
            id = UUID.randomUUID().toString(),
            destination = browseButton.text,
        )
    }
}

class BackgroundRenderingJPanel(val color: Color) : JPanel() {
    init {
        isOpaque = false
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        g.color = color
        (g as? Graphics2D)?.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        g.fillRoundRect(0, 0, width - 1, height - 1, 20, 20)

    }

}