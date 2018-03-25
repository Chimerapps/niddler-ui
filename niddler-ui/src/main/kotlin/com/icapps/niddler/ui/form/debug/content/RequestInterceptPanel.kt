package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.lib.debugger.model.BaseMatcher
import com.icapps.niddler.lib.debugger.model.LocalRequestIntercept
import com.icapps.niddler.lib.debugger.model.ModifiableDebuggerConfiguration
import com.icapps.niddler.ui.left
import com.icapps.niddler.ui.plusAssign
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JPanel

/**
 * @author nicolaverbeeck
 */
class RequestInterceptPanel(configuration: ModifiableDebuggerConfiguration,
                            changeListener: () -> Unit)
    : BaseOverridePanel<BaseMatcher>(configuration, changeListener) {

    private val staticOverrideToggle = JCheckBox("Default response")
    private val defaultResponsePanel = JPanel()

    init {
        staticOverrideToggle.addActionListener {
            defaultResponsePanel.isVisible = staticOverrideToggle.isSelected
            if (!duringInit)
                changeListener()
        }

        initUI()
    }

    override fun initContent(box: Box, matchingPanel: JPanel) {
        super.initContent(box, matchingPanel)

        box += Box.createVerticalStrut(8)
        box += staticOverrideToggle.left()
        box += Box.createVerticalStrut(8)
        box += defaultResponsePanel
    }

    fun init(requestIntercept: LocalRequestIntercept, enabled: Boolean) {
        initStarting()

        initMatching(requestIntercept)
        enabledFlag.isSelected = enabled

        initComplete()
    }

    override fun onEnableStateChanged(selected: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun applyToModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}