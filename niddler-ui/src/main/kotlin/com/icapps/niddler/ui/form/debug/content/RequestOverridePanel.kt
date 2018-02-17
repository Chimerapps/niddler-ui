package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.ui.debugger.model.ModifiableDebuggerConfiguration
import com.icapps.niddler.ui.debugger.model.RequestOverride
import com.icapps.niddler.ui.left
import java.awt.BorderLayout
import java.awt.Dimension
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
    private val methodField = JTextField()

    private var request: RequestOverride? = null

    init {
        regexField.maximumSize = Dimension(regexField.maximumSize.width, regexField.preferredSize.height)
        methodField.maximumSize = Dimension(methodField.maximumSize.width, methodField.preferredSize.height)

        border = EmptyBorder(6, 12, 6, 12)

        val matchingPanel = JPanel()
        matchingPanel.layout = BoxLayout(matchingPanel, BoxLayout.Y_AXIS)
        matchingPanel.border = TitledBorder("Matching")

        enabledFlag.addActionListener {
            request?.let {
                configuration.setBlacklistActive(it.id, enabledFlag.isSelected)
            }
        }

        matchingPanel.add(JLabel("Url regex").left())
        matchingPanel.add(regexField.left())
        matchingPanel.add(JLabel("Method").left())
        matchingPanel.add(methodField.left())

        val box = Box.createVerticalBox()

        box.add(enabledFlag.left())
        box.add(Box.createVerticalStrut(8))
        box.add(matchingPanel.left())

        add(box)
    }

    fun init(override: RequestOverride, checked: Boolean) {
        this.request = override

        enabledFlag.isSelected = checked
    }

    override fun applyToModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}