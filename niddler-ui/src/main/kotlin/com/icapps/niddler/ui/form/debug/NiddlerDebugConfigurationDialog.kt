package com.icapps.niddler.ui.form.debug

import com.icapps.niddler.ui.form.debug.content.DelaysConfigurationPanel
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

    fun init()

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