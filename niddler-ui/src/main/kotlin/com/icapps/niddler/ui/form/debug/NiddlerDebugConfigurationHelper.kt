package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.debugger.model.ModifiableDebuggerConfiguration
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.debug.dialog.EnterRegexDialog
import com.icapps.niddler.ui.util.logger
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

    private companion object {
        private val log = logger<NiddlerDebugConfigurationHelper>()
    }

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

    override fun onAddRequestOverride() {
        try {
            val id = configurationConfiguration.addRequestOverride(null, null, false)
            val node = configurationModel.configurationRoot.requestOverrideRoot.findNode {
                it.requestOverride.id == id
            }
            if (node != null)
                configurationDialog.focusOnNode(node)
        } catch (e: Exception) {
            showError(e)
        }
    }


    override fun addResponseInterceptor() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAddResponseOverride() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRemoveClicked() {
        configurationDialog.removeCurrentItem()
    }

    protected open fun showError(e: Exception) {
        log.error("Failed to execute action", e)
        JOptionPane.showMessageDialog(owner, e.message)
    }
}