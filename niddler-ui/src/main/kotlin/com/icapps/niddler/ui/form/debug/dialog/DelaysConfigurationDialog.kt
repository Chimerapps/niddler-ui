package com.icapps.niddler.ui.form.debug.dialog

import com.icapps.niddler.ui.addChangeListener
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.Dialog
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Window
import java.text.NumberFormat
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.NumberFormatter


/**
 * @author nicolaverbeeck
 */
class DelaysConfigurationDialog(owner: Window?, componentsFactory: ComponentsFactory) {

    private val preBlacklist = DelayPanel(this, "Before blacklist")
    private val postBlacklist = DelayPanel(this, "After blacklist")
    private val ensureCallTime = DelayPanel(this, "Ensure call time")
    private val dialog: Dialog = componentsFactory.createDialog(owner, "Simulated delays", createContent())

    init {
        dialog.dialogCanResize = false
    }

    private fun createContent(): JComponent {
        val rootBox = Box.createVerticalBox()

        rootBox.add(preBlacklist)
        rootBox.add(Box.createVerticalStrut(6))
        rootBox.add(postBlacklist)
        rootBox.add(Box.createVerticalStrut(6))
        rootBox.add(ensureCallTime)

        rootBox.border = EmptyBorder(6, 12, 6, 12)

        return rootBox
    }

    fun show(previousConfiguration: DebuggerDelays?): DebuggerDelays? {
        set(preBlacklist, previousConfiguration?.preBlacklist)
        set(postBlacklist, previousConfiguration?.postBlacklist)
        set(ensureCallTime, previousConfiguration?.timePerCall)

        if (dialog.show(::validateInput) == Dialog.ACCEPTED)
            return extractDelays()
        return null
    }

    private fun onApply() {
        dialog.tryValidate()
    }

    private fun extractDelays(): DebuggerDelays {
        val pre = extractTime(preBlacklist)
        val post = extractTime(postBlacklist)
        val ensureTime = extractTime(ensureCallTime)

        return DebuggerDelays(pre, post, ensureTime)
    }

    private fun extractTime(panel: DelayPanel): Long? {
        if (panel.checkbox.isSelected)
            return panel.field.text.toLongOrNull()
        return null
    }

    private fun set(panel: DelayPanel, time: Long?) {
        panel.checkbox.isSelected = time != null
        panel.field.text = time?.toString() ?: ""
    }

    private class DelayPanel(private val parentDialog: DelaysConfigurationDialog,
                             title: String) : JPanel(BorderLayout()) {

        val checkbox: JCheckBox = JCheckBox()
        val field: JFormattedTextField = JFormattedTextField(numberFormatter())

        init {
            add(JLabel(title), BorderLayout.NORTH)

            field.apply {
                maximumSize = Dimension(maximumSize.width, preferredSize.height)
                addActionListener { parentDialog.onApply() }
            }
            field.addChangeListener { checkbox.isSelected = it.text.isNotEmpty() }

            add(checkbox, BorderLayout.WEST)
            add(field, BorderLayout.CENTER)

            preferredSize = Dimension(300, preferredSize.height)
            minimumSize = Dimension(300, minimumSize.height)
        }


        private fun numberFormatter(): NumberFormatter {
            val format = NumberFormat.getIntegerInstance()
            format.isGroupingUsed = false
            val formatter = object : NumberFormatter(format) {
                override fun stringToValue(text: String?): Any? {
                    if (text.isNullOrBlank())
                        return null
                    return text!!.toLong()
                }

                override fun valueToString(value: Any?): String {
                    if (value == null)
                        return ""
                    return value.toString()
                }
            }
            formatter.valueClass = Long::class.java
            formatter.minimum = 0L
            formatter.maximum = Long.MAX_VALUE
            formatter.allowsInvalid = false
            return formatter
        }
    }

    private fun validateInput() = Dialog.ValidationResult(true)

}