package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.lib.debugger.model.ModifiableDebuggerConfiguration
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * @author nicolaverbeeck
 */
class BlacklistPanel(private val configuration: ModifiableDebuggerConfiguration) : JPanel(), ContentPanel {

    private val editField = JTextField()

    private lateinit var startRegex: String

    private val enabledFlag: JCheckBox = JCheckBox("Enabled")

    init {
        editField.maximumSize = Dimension(editField.maximumSize.width, editField.preferredSize.height)

        val box = Box.createVerticalBox()
        layout = BorderLayout()
        box.border = EmptyBorder(10, 0, 0, 5)
        box.add(JLabel("Regular expression").apply {
            alignmentX = JComponent.LEFT_ALIGNMENT
            border = EmptyBorder(0, 5, 0, 0)
        })

        box.add(enabledFlag)
        enabledFlag.addActionListener {
            configuration.setBlacklistActive(editField.text.trim(), enabledFlag.isSelected)
        }

        val horizontalBox = Box.createHorizontalBox()
        horizontalBox.alignmentX = JComponent.LEFT_ALIGNMENT

        horizontalBox.add(editField)

        box.add(horizontalBox)
        add(box)
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        enabledFlag.isSelected = enabled
        configuration.setBlacklistActive(startRegex, enabled)
    }

    fun init(regex: String, checked: Boolean) {
        startRegex = regex
        editField.text = startRegex
        enabledFlag.isSelected = checked
    }

    override fun applyToModel() {
        val inEditor = editField.text.trim()
        if (inEditor == startRegex) {
            return
        }

        configuration.changeRegex(startRegex, inEditor)
        startRegex = inEditor
    }
}