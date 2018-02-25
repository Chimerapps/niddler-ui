package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.*
import com.icapps.niddler.ui.debugger.model.DebugRequest
import com.icapps.niddler.ui.debugger.model.LocalRequestOverride
import com.icapps.niddler.ui.debugger.model.ModifiableDebuggerConfiguration
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

    private var request: LocalRequestOverride? = null

    private val staticRequestPanel = JPanel()

    private val newUrlField = JTextField()
    private val newMethodField = JTextField()
    private val newHeadersField = HeaderEditorPanel(changeListener)

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
        staticRequestPanel += JLabel("Headers").left().offsetLeft()
        staticRequestPanel += newHeadersField.left().apply { border = EmptyBorder(4, 4, 0, 6) }
        staticRequestPanel += Box.createVerticalStrut(6)

        box += Box.createVerticalStrut(8)
        box += staticRequestToggle.left()
        box += Box.createVerticalStrut(8)
        box += staticRequestPanel

        add(box)
    }

    fun init(override: LocalRequestOverride, checked: Boolean) {
        this.request = override

        enabledFlag.isSelected = checked
        staticRequestToggle.isSelected = override.debugRequest != null
        staticRequestPanel.isVisible = staticRequestToggle.isSelected

        regexField.text = override.regex ?: ""
        methodField.editor.item = override.matchMethod ?: ""

        override.debugRequest?.let {
            newUrlField.text = it.url
            newMethodField.text = it.method
            newHeadersField.init(it.headers)
        }
    }

    override fun applyToModel() {
        val request = request ?: return

        val regex = regexField.text.trim()
        val method = methodField.selectedItem.toString().trim()

        if (regex.isEmpty() && method.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matcher defined")
            return
        }

        var override: DebugRequest? = null
        if (staticRequestToggle.isSelected) {
            val newUrl = newUrlField.text.trim()
            val newMethod = newMethodField.text.trim()
            val headers = newHeadersField.extractHeaders()
            if (newUrl.isEmpty()) {
                JOptionPane.showMessageDialog(this, "New url required")
                return
            }
            if (newMethod.isEmpty()) {
                JOptionPane.showMessageDialog(this, "New method required")
                return
            }
            override = DebugRequest(newUrl, newMethod, headers, encodedBody = null, bodyMimeType = null)
        }
        request.regex = if (regex.isEmpty()) null else regex
        request.matchMethod = if (method.isEmpty()) null else method
        request.debugRequest = override
        configuration.modifyRequestOverrideAction(request, enabledFlag.isSelected)
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}