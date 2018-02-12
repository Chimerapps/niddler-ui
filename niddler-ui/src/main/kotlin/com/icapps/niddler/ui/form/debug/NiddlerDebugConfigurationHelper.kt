package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.debugger.model.ModifiableDebuggerConfiguration
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.debug.dialog.EnterRegexDialog
import java.awt.Window
import javax.swing.JOptionPane

/**
 * @author nicolaverbeeck
 */
open class NiddlerDebugConfigurationHelper(private val owner: Window?,
                                           private val factory: ComponentsFactory,
                                           private val configurationDialog: NiddlerDebugConfigurationDialog,
                                           private val configurationConfiguration: ModifiableDebuggerConfiguration,
                                           private val configurationModel: ConfigurationModel)
    : DebugToolbar.DebugToolbarListener {

    override fun onAddBlacklist() {
        val regex = EnterRegexDialog(owner, "Add new blacklist item", factory).show() ?: return

        try {
            configurationConfiguration.addBlacklistItem(regex, true)
            val node = configurationModel.configurationRoot.blacklistRoot.findNode {
                it.regex == regex
            }
            if (node != null)
                configurationDialog.focusOnNode(node)
        } catch (e: Exception) {
            showError(e)
        }
    }

    override fun onAddRequestInterceptor() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAddRequestOverride() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAddResponseOverride() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRemoveClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    protected open fun showError(e: Exception) {
        JOptionPane.showMessageDialog(owner, e.message)
    }
}