package com.icapps.niddler.ui.form

import javax.swing.JMenuItem
import javax.swing.JPopupMenu

/**
 * Created by maartenvangiel on 20/04/2017.
 */
class NiddlerMessagePopupMenu(val listener: Listener) : JPopupMenu() {

    private val copyUrlItem: JMenuItem = JMenuItem("Copy URL")
    private val copyBodyItem: JMenuItem = JMenuItem("Copy body")

    init {
        add(copyUrlItem.apply {
            addActionListener { listener.onCopyUrlClicked() }
        })
        add(copyBodyItem.apply {
            addActionListener { listener.onCopyBodyClicked() }
        })
    }

    interface Listener {
        fun onCopyUrlClicked()
        fun onCopyBodyClicked()
    }

}