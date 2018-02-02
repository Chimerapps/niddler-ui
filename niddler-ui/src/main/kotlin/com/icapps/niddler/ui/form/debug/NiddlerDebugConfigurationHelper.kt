package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.debugger.model.DebuggerInterface
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.debug.dialog.DelaysConfigurationDialog
import java.awt.Window

/**
 * @author nicolaverbeeck
 */
class NiddlerDebugConfigurationHelper(private val owner: Window?,
                                      private val factory: ComponentsFactory,
                                      private val debuggerInterface: DebuggerInterface)
    : DebugToolbar.DebugToolbarListener {

    override fun onAddBlacklist() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun onConfigureDelays() {
        val delays = DelaysConfigurationDialog(owner, factory).show(debuggerInterface.debugDelays()) ?: return
        debuggerInterface.updateDelays(delays)
    }

    override fun onRemoveClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}