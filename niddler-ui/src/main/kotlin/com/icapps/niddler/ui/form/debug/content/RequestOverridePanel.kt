package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.lib.debugger.model.BaseMatcher
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.LocalRequestOverride
import com.icapps.niddler.lib.debugger.model.ModifiableDebuggerConfiguration
import com.icapps.niddler.ui.left
import com.icapps.niddler.ui.offsetLeft
import com.icapps.niddler.ui.plusAssign
import com.icapps.niddler.ui.singleLine
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * @author nicolaverbeeck
 */
class RequestOverridePanel(configuration: ModifiableDebuggerConfiguration,
                           changeListener: () -> Unit)
    : BaseOverridePanel<BaseMatcher>(configuration, changeListener) {

    private var request: LocalRequestOverride? = null

    private val staticRequestPanel = JPanel()

    private val newUrlField = JTextField()
    private val newMethodField = JTextField()
    private val newHeadersField = HeaderEditorPanel(changeListener)

    private val staticRequestToggle = JCheckBox("Static override")

    init {
        newUrlField.singleLine()
        newMethodField.singleLine()

        staticRequestToggle.addActionListener {
            staticRequestPanel.isVisible = staticRequestToggle.isSelected
            if (!duringInit)
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

        initUI()
    }

    override fun onEnableStateChanged(selected: Boolean) {
        request?.let {
            configuration.setRequestOverrideActive(it.id, selected)
        }
    }

    override fun initContent(box: Box, matchingPanel: JPanel) {
        super.initContent(box, matchingPanel)
        box += Box.createVerticalStrut(8)
        box += staticRequestToggle.left()
        box += Box.createVerticalStrut(8)
        box += staticRequestPanel
    }

    fun init(override: LocalRequestOverride, checked: Boolean) {
        initStarting()

        initMatching(override)
        this.request = override

        enabledFlag.isSelected = checked
        staticRequestToggle.isSelected = override.debugRequest != null
        staticRequestPanel.isVisible = staticRequestToggle.isSelected

        override.debugRequest?.let {
            newUrlField.text = it.url
            newMethodField.text = it.method
            newHeadersField.init(it.headers)
        }

        initComplete()
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
        enabledFlag.isSelected = enabled
        onEnableStateChanged(enabled)
    }

}