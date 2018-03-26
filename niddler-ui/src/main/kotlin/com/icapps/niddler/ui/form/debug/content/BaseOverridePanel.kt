package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.*
import com.icapps.niddler.lib.debugger.model.BaseMatcher
import com.icapps.niddler.lib.debugger.model.ModifiableDebuggerConfiguration
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

/**
 * @author nicolaverbeeck
 */
abstract class BaseOverridePanel<T : BaseMatcher>(protected val configuration: ModifiableDebuggerConfiguration,
                                                  changeListener: () -> Unit) : JPanel(BorderLayout()), ContentPanel {

    protected val enabledFlag: JCheckBox = JCheckBox("Enabled")
    protected val regexField = JTextField()
    protected val methodField = JComboBox(arrayOf("", "GET", "PUT", "POST", "DELETE", "HEAD", "OPTIONS"))
    protected var duringInit = false

    init {
        regexField.singleLine()
        methodField.singleLine()
        methodField.isEditable = true

        border = EmptyBorder(6, 12, 6, 12)


        enabledFlag.addActionListener {
            if (!duringInit)
                onEnableStateChanged(enabledFlag.isSelected)
        }
        methodField.addActionListener {
            if (!duringInit)
                changeListener()
        }
        regexField.addChangeListener {
            if (!duringInit)
                changeListener()
        }
        (methodField.editor.editorComponent as? JTextField)?.addChangeListener {
            if (!duringInit)
                changeListener()
        }
    }

    protected fun initUI() {
        val matchingPanel = JPanel()
        matchingPanel.layout = BoxLayout(matchingPanel, BoxLayout.Y_AXIS)
        matchingPanel.border = TitledBorder("Matching")

        val box = Box.createVerticalBox()

        initMatchingPanel(matchingPanel)
        initContent(box, matchingPanel)

        add(box)
    }

    protected fun initStarting() {
        duringInit = true
    }

    protected fun initComplete() {
        duringInit = false
    }

    protected open fun initMatchingPanel(matchingPanel: JPanel) {
        matchingPanel += JLabel("Url regex").left().offsetLeft()
        matchingPanel += regexField.left()
        matchingPanel += JLabel("Method").left().offsetLeft()
        matchingPanel += methodField.left()
    }

    protected open fun initContent(box: Box, matchingPanel: JPanel) {
        box += enabledFlag.left()
        box += Box.createVerticalStrut(8)
        box += matchingPanel.left()
    }

    protected abstract fun onEnableStateChanged(selected: Boolean)

    protected fun initMatching(matcher: T) {
        initStarting()

        methodField.editor.item = matcher.matchMethod ?: ""
        regexField.text = matcher.regex ?: ""
    }

}