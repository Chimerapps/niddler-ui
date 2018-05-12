package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

/**
 * Created by maartenvangiel on 20/04/2017.
 */
open class NiddlerMessagePopupMenu<T : NiddlerMessagePopupMenu.Listener>(val listener: T) : JPopupMenu() {

    private val copyUrlItem: JMenuItem = JMenuItem("Copy URL")
    private val copyBodyItem: JMenuItem = JMenuItem("Copy body")
    private val exportCurlItem: JMenuItem = JMenuItem("Export cUrl request")

    init {
        add(copyUrlItem.apply {
            addActionListener { listener.onCopyUrlClicked() }
        })
        add(copyBodyItem.apply {
            addActionListener { listener.onCopyBodyClicked() }
        })
        add(exportCurlItem.apply {
            addActionListener { listener.onExportCurlRequestClicked() }
        })
    }

    final override fun add(comp: JMenuItem?): JMenuItem {
        return super.add(comp)
    }

    interface Listener {
        fun onCopyUrlClicked()
        fun onCopyBodyClicked()
        fun onExportCurlRequestClicked()
    }

}

/**
 * @author Nicola Verbeeck
 */
class NiddlerTableMessagePopupMenu(listener: Listener)
    : NiddlerMessagePopupMenu<NiddlerTableMessagePopupMenu.Listener>(listener) {

    private val showOtherMenuItem: JMenuItem = JMenuItem("")
    private var source: ParsedNiddlerMessage? = null

    init {
        add(showOtherMenuItem.apply {
            addActionListener { listener.onShowRelatedClicked(source) }
        })
    }

    fun setOtherText(text: String, source: ParsedNiddlerMessage) {
        if (showOtherMenuItem.parent == null)
            add(showOtherMenuItem)

        showOtherMenuItem.text = text
        this.source = source
    }

    fun clearExtra() {
        if (showOtherMenuItem.parent != null)
            remove(showOtherMenuItem)
    }

    interface Listener : NiddlerMessagePopupMenu.Listener {
        fun onShowRelatedClicked(source: ParsedNiddlerMessage?)
    }

}