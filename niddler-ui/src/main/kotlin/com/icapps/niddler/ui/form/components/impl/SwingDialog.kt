package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.ui.form.components.Dialog
import java.awt.Window
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog

/**
 * @author nicolaverbeeck
 */
class SwingDialog(parent: Window?, title: String, content: JComponent) : Dialog,
        JDialog(parent, title, ModalityType.APPLICATION_MODAL) {

    private companion object {
        private const val RESULT_CANCEL = 1
        private const val RESULT_OK = 0
    }

    var resultCode: Int = -1
    override var dialogCanResize: Boolean
        get() = isResizable
        set(value) {
            isResizable = value
        }
    private lateinit var inputValidator: () -> Dialog.ValidationResult

    init {
        val linearBox = Box.createVerticalBox()
        linearBox.add(content)
        linearBox.add(Box.createVerticalGlue())

        val buttonBox = Box.createHorizontalBox()
        buttonBox.add(Box.createHorizontalGlue())
        buttonBox.add(JButton("Cancel").apply {
            addActionListener {
                resultCode = RESULT_CANCEL
                this@SwingDialog.isVisible = false
            }
        })
        buttonBox.add(JButton("OK").apply {
            addActionListener {
                if (inputValidator().success) {
                    resultCode = RESULT_OK
                    this@SwingDialog.isVisible = false
                }
            }
        })

        linearBox.add(buttonBox)
        rootPane.contentPane = linearBox
        pack()
        if (parent != null)
            setLocationRelativeTo(parent)
    }

    override fun tryValidate() {
        if (inputValidator().success) {
            resultCode = RESULT_OK
            this@SwingDialog.isVisible = false
        }
    }

    override fun show(inputValidator: () -> Dialog.ValidationResult): Int {
        this.inputValidator = inputValidator
        isVisible = true
        dispose()
        if (resultCode == RESULT_OK)
            return Dialog.ACCEPTED
        return Dialog.CANCELLED
    }
}