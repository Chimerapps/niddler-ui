package com.icapps.niddler.ui.util

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.ImageIcon

/**
 * @author nicolaverbeeck
 */
fun simpleAction(title: String = "", icon: String? = null, listener: (event: ActionEvent) -> Unit): Action {
    return object : AbstractAction(title, icon?.loadIcon<Any>()) {
        override fun actionPerformed(e: ActionEvent) {
            listener(e)
        }
    }
}

fun Any.loadIcon(path: String): ImageIcon {
    return ImageIcon(javaClass.getResource(path))
}

inline fun <reified T> String.loadIcon(): ImageIcon {
    return ImageIcon(T::class.java.getResource(this))
}
