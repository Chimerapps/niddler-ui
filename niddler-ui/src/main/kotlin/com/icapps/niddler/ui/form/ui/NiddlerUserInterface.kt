package com.icapps.niddler.ui.form.ui

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.NiddlerToolbar
import com.icapps.niddler.ui.model.MessageContainer
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */

interface NiddlerUserInterface {

    fun init(messageContainer: MessageContainer)

    var connectButtonListener: (() -> Unit)?
    var filterListener: ((String?) -> Unit)?
    var disconnectButtonListener: (() -> Unit)?

    val toolbar: NiddlerToolbar
    val disconnectButton: Component

    fun setStatusText(statusText: String?)
    fun setStatusIcon(icon: ImageIcon?)

    val asComponent: JComponent
    val componentsFactory: ComponentsFactory

    val overview: NiddlerOverviewUserInterface
    val detail: NiddlerDetailUserInterface
}
