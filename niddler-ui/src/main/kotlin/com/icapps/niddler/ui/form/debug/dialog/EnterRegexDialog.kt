package com.icapps.niddler.ui.form.debug.dialog

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.Dialog
import java.awt.Dimension
import java.awt.Window
import java.util.regex.Pattern
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

/**
 * @author nicolaverbeeck
 */
class EnterRegexDialog(owner: Window?, title: String, componentsFactory: ComponentsFactory) {

    private lateinit var textField: JTextField
    private val dialog: Dialog = componentsFactory.createDialog(owner, title, createContent())

    init {
        dialog.dialogCanResize = false
    }

    private fun createContent(): JComponent {
        val box = Box.createVerticalBox()

        val internal = Box.createVerticalBox()
        internal.border = EmptyBorder(14, 8, 14, 8)
        internal.add(JLabel("Regular expression"))
        textField = JTextField().apply {
            maximumSize = Dimension(maximumSize.width, preferredSize.height)
            addActionListener { dialog.tryValidate() }
        }
        internal.add(textField)

        box.add(internal)
        box.add(Box.createVerticalGlue())

        return box
    }

    fun show(): String? {
        if (dialog.show(this::validateInput) == Dialog.ACCEPTED)
            return textField.text.trim()
        return null
    }

    private fun validateInput(): Dialog.ValidationResult {
        return try {
            val trimmed = textField.text.trim()
            if (trimmed.isEmpty())
                Dialog.ValidationResult(false, "Empty regex not allowed")

            Pattern.compile(trimmed)
            Dialog.ValidationResult(true)
        } catch (e: Exception) {
            Dialog.ValidationResult(false, e.message)
        }
    }

}