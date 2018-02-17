package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.*
import com.icapps.niddler.ui.debugger.model.ModifiableDebuggerConfiguration
import com.icapps.niddler.ui.debugger.model.RequestOverride
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

/**
 * @author nicolaverbeeck
 */
class RequestOverridePanel(private var configuration: ModifiableDebuggerConfiguration,
                           changeListener: () -> Unit) : JPanel(BorderLayout()), ContentPanel {

    private val enabledFlag: JCheckBox = JCheckBox("Enabled")
    private val regexField = JTextField()
    private val methodField = JComboBox(arrayOf("", "GET", "PUT", "POST", "DELETE", "HEAD", "OPTIONS"))

    private var request: RequestOverride? = null

    private val staticRequestPanel = JPanel()
    private val newUrlField = JTextField()
    private val newMethodField = JTextField()
    private val staticRequestToggle = JCheckBox("Static override")

    init {
        regexField.singleLine()
        methodField.singleLine()
        newUrlField.singleLine()
        newMethodField.singleLine()

        methodField.isEditable = true

        border = EmptyBorder(6, 12, 6, 12)

        val matchingPanel = JPanel()
        matchingPanel.layout = BoxLayout(matchingPanel, BoxLayout.Y_AXIS)
        matchingPanel.border = TitledBorder("Matching")

        enabledFlag.addActionListener {
            request?.let {
                configuration.setRequestOverrideActive(it.id, enabledFlag.isSelected)
            }
        }
        methodField.addActionListener {
            changeListener()
        }
        regexField.addChangeListener {
            changeListener()
        }
        (methodField.editor.editorComponent as? JTextField)?.addChangeListener {
            changeListener()
        }

        matchingPanel += JLabel("Url regex").left().offsetLeft()
        matchingPanel += regexField.left()
        matchingPanel += JLabel("Method").left().offsetLeft()
        matchingPanel += methodField.left()

        val box = Box.createVerticalBox()

        box += enabledFlag.left()
        box += Box.createVerticalStrut(8)
        box += matchingPanel.left()

        staticRequestToggle.addActionListener {
            staticRequestPanel.isVisible = staticRequestToggle.isSelected
            changeListener()
        }

        staticRequestPanel.border = UIManager.getBorder("TitledBorder.border")
        staticRequestPanel.layout = BoxLayout(staticRequestPanel, BoxLayout.Y_AXIS)
        staticRequestPanel += JLabel("New url").left().offsetLeft()
        staticRequestPanel += newUrlField.left()
        staticRequestPanel += JLabel("New method").left().offsetLeft()
        staticRequestPanel += newMethodField.left()
        staticRequestPanel += Box.createVerticalStrut(4)

        box += Box.createVerticalStrut(8)
        box += staticRequestToggle.left()
        box += Box.createVerticalStrut(8)
        box += staticRequestPanel

        add(box)
    }

    fun init(override: RequestOverride, checked: Boolean) {
        this.request = override

        enabledFlag.isSelected = checked
        staticRequestToggle.isSelected = override.debugRequest != null
        staticRequestPanel.isVisible = staticRequestToggle.isSelected
    }

    override fun applyToModel() {
        val regex = regexField.text.trim()
        val method = methodField.selectedItem.toString().trim()

        if (regex.isEmpty() && method.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matcher defined")
            return
        }
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}