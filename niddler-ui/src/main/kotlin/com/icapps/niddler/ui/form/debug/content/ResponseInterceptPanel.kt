package com.icapps.niddler.ui.form.debug.content

import com.icapps.niddler.lib.debugger.model.BaseMatcher
import com.icapps.niddler.lib.debugger.model.LocalResponseIntercept
import com.icapps.niddler.lib.debugger.model.ModifiableDebuggerConfiguration
import com.icapps.niddler.ui.form.ComponentsFactory
import javax.swing.Box
import javax.swing.JOptionPane
import javax.swing.JPanel

/**
 * @author nicolaverbeeck
 */
class ResponseInterceptPanel(configuration: ModifiableDebuggerConfiguration,
                             componentsFactory: ComponentsFactory,
                             changeListener: () -> Unit)
    : BaseOverridePanel<BaseMatcher>(configuration, changeListener) {

    private var response: LocalResponseIntercept? = null

    init {
        initUI()
    }

    fun init(override: LocalResponseIntercept, checked: Boolean) {
        initStarting()

        initMatching(override)
        response = override

        enabledFlag.isSelected = checked

        initComplete()
    }

    override fun onEnableStateChanged(selected: Boolean) {
        response?.let {
            configuration.setResponseInterceptActive(it.id, selected)
        }
    }

    override fun applyToModel() {
        val response = response ?: return

        val regex = regexField.text.trim()
        val method = methodField.selectedItem.toString().trim()

        if (regex.isEmpty() && method.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matcher defined")
            return
        }
        response.regex = if (regex.isEmpty()) null else regex
        response.matchMethod = if (method.isEmpty()) null else method
        configuration.modifyResponseIntercept(response, enabledFlag.isSelected)
    }

    override fun updateEnabledFlag(enabled: Boolean) {
        enabledFlag.isSelected = enabled
        onEnableStateChanged(enabled)
    }
}