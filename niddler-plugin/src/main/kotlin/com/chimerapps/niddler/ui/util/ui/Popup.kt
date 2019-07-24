package com.chimerapps.niddler.ui.util.ui

import com.intellij.openapi.ui.JBPopupMenu
import javax.swing.JMenuItem

class Popup : JBPopupMenu {

    constructor(vararg popupActions: PopupAction) : super() {
        popupActions.forEach { action ->
            add(JMenuItem(action.text).also { menu -> menu.addActionListener { action.action() } })
        }
    }

    constructor(popupActions: List<PopupAction>) : super() {
        popupActions.forEach { action ->
            add(JMenuItem(action.text).also { menu -> menu.addActionListener { action.action() } })
        }
    }

}

class PopupAction(val text: String, val action: () -> Unit)

@Suppress("NOTHING_TO_INLINE")
inline infix fun String.action(noinline action: () -> Unit): PopupAction {
    return PopupAction(this, action)
}