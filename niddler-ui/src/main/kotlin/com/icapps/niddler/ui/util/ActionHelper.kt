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
    return object : AbstractAction(title, icon?.loadIcon<Any>()) {
        override fun actionPerformed(e: ActionEvent) {
            listener(e)
        }
    }
}

interface ImageHelper {
    fun loadImage(clazz: Class<*>, path: String): Icon
}

lateinit var iconLoader: ImageHelper

fun Any.loadIcon(path: String): Icon {
    return iconLoader.loadImage(javaClass, path)
}

inline fun <reified T> String.loadIcon(): Icon {
    return iconLoader.loadImage(T::class.java, this)
}

class SwingImageHelper : ImageHelper {

    override fun loadImage(clazz: Class<*>, path: String): ImageIcon {
        return ImageIcon(clazz.getResource(path))
    }
}