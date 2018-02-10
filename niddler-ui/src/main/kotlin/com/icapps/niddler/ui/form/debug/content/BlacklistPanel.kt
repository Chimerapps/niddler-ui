package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.debugger.model.saved.DisableableItem
import com.icapps.niddler.ui.debugger.model.saved.TemporaryDebuggerConfiguration
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * @author nicolaverbeeck
 */
class BlacklistPanel(private val configuration: TemporaryDebuggerConfiguration) : JPanel(), ContentPanel {

    private val editField = JTextField()
    private val testEditField = JTextField()

    private lateinit var startRegex: String
    private var item: DisableableItem<String>? = null

    private val enabledFlag: JCheckBox = JCheckBox("Enabled")
    override var enableListener: ((enabled: Boolean) -> Unit)? = null

    init {
        editField.maximumSize = Dimension(editField.maximumSize.width, editField.preferredSize.height)
        testEditField.maximumSize = Dimension(testEditField.maximumSize.width, testEditField.preferredSize.height)

        val box = Box.createVerticalBox()
        layout = BorderLayout()
        box.border = EmptyBorder(10, 0, 0, 5)
        box.add(JLabel("Regular expression").apply {
            alignmentX = JComponent.LEFT_ALIGNMENT
            border = EmptyBorder(0, 5, 0, 0)
        })

        box.add(enabledFlag)
        enabledFlag.addActionListener { enableListener?.invoke(enabledFlag.isSelected) }

        val horizontalBox = Box.createHorizontalBox()
        horizontalBox.alignmentX = JComponent.LEFT_ALIGNMENT

        horizontalBox.add(editField)

        box.add(horizontalBox)
        box.add(Box.createGlue())
        add(box)
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        enabledFlag.isSelected = enabled
    }

    fun init(item: DisableableItem<String>) {
        startRegex = item.item
        editField.text = startRegex
        enabledFlag.isSelected = item.enabled
        this.item = item
    }

    fun initNew() {
        startRegex = ""
        item = null
        editField.text = startRegex
        enabledFlag.isSelected = true
    }

    override fun apply(isEnabled: Boolean) {
        val inEditor = editField.text.trim()
        if (inEditor == startRegex) {
            item?.let { it.enabled = isEnabled }
            return
        }

        item?.let { configuration.removeBlacklistItem(it) }
        item = configuration.addBlacklistItem(inEditor, isEnabled)
        startRegex = inEditor
    }
}