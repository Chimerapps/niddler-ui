package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.ui.form.debug.nodes.ConfigurationNode
import javax.swing.JPanel
import javax.swing.JTree

/**
 * @author nicolaverbeeck
 */
interface NiddlerDebugConfigurationDialog {

    var visibility: Boolean

    val debugToolbar: DebugToolbar
    val configurationTree: JTree
    val detailPanelContainer: JPanel

    val configurationModel: ConfigurationModel

    fun init(applyListener: (DebuggerConfiguration) -> Unit)

    fun focusOnNode(node: ConfigurationNode<*>)

    fun removeCurrentItem()

}

interface DebugToolbar {

    var listener: DebugToolbarListener?

    fun setRemoveEnabled(enabled: Boolean)

    interface DebugToolbarListener {

        fun onAddBlacklist()

        fun addResponseInterceptor()

        fun onAddRequestOverride()

        fun onAddResponseOverride()

        fun onRemoveClicked()

    }

}