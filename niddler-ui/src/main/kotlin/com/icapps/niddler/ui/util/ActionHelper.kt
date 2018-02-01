package com.icapps.niddler.ui.util

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * @author nicolaverbeeck
 */
fun simpleAction(title: String = "", icon: String? = null, listener: (event: ActionEvent) -> Unit): Action {
    return object : AbstractAction(title, icon?.loadIcon(listener.javaClass)) {
        override fun actionPerformed(e: ActionEvent) {
            listener(e)
        }
    }
}

fun String.loadIcon(classContext: Class<*>): Icon {
    return ImageIcon(classContext.getResource(this))
}