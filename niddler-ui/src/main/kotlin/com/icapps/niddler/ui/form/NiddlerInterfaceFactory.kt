package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.form.components.NiddlerToolbar
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.JTree

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */

interface NiddlerUserInterface {

    fun init()

    var connectButtonListener: (() -> Unit)?

    val messagesAsTable: JTable
    val messagesAsTree: JTree
    val toolbar: NiddlerToolbar

    fun setStatusText(statusText: String?)
    fun setStatusIcon(icon: ImageIcon?)

    val asComponent: JComponent
    val componentsFactory: ComponentsFactory
}
