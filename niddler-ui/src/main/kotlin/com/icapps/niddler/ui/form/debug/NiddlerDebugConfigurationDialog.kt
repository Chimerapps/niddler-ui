package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.ui.form.debug.nodes.TreeNode
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

    fun focusOnNode(node: TreeNode)

}

interface DebugToolbar {

    var listener: DebugToolbarListener?

    fun setRemoveEnabled(enabled: Boolean)

    interface DebugToolbarListener {

        fun onAddBlacklist()

        fun onAddRequestInterceptor()

        fun onAddRequestOverride()

        fun onAddResponseOverride()

        fun onRemoveClicked()

    }

}