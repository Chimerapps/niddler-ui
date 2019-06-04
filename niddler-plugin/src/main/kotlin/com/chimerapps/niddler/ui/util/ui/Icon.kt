package com.chimerapps.niddler.ui.util.ui

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

fun Any.loadIcon(path: String): Icon {
    return IconLoader.getIcon(path, javaClass)
}
