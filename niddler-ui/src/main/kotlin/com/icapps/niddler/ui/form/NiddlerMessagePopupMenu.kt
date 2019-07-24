package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.NiddlerMessageStorage
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
class NiddlerTableMessagePopupMenu(private val baseUrlHider: BaseUrlHider,
                                   private val messages: NiddlerMessageStorage<ParsedNiddlerMessage>,
                                   listener: Listener)
    : NiddlerMessagePopupMenu<NiddlerTableMessagePopupMenu.Listener>(listener) {

    private val showOtherMenuItem: JMenuItem = JMenuItem("")
    private val baseUrlMenuItem = JMenuItem("")
    private var source: ParsedNiddlerMessage? = null

    init {
        add(showOtherMenuItem.apply {
            addActionListener { listener.onShowRelatedClicked(source) }
        })
        add(baseUrlMenuItem.apply {
            addActionListener { listener.onUpdateBaseUrlClicked(source) }
        })
    }

    fun setOtherText(text: String) {
        clearExtra() //Ensure consistent ordering
        add(showOtherMenuItem)

        showOtherMenuItem.text = text
    }

    fun clearExtra() {
        if (showOtherMenuItem.parent != null)
            remove(showOtherMenuItem)
    }

    fun updateBaseUrlOptions(row: ParsedNiddlerMessage?) {
        this.source = row

        if (baseUrlMenuItem.parent != null)
            remove(baseUrlMenuItem)
        row ?: return

        val url = if (row.isRequest) row.url
        else
            messages.findRequest(row)?.url
        url ?: return

        val hiddenBase = baseUrlHider.getHiddenBaseUrl(url)
        if (hiddenBase == null) {
            baseUrlMenuItem.text = "Hide base urls"
        } else {
            baseUrlMenuItem.text = "Show base urls"
        }
        add(baseUrlMenuItem)
    }

    interface Listener : NiddlerMessagePopupMenu.Listener {
        fun onShowRelatedClicked(source: ParsedNiddlerMessage?)
        fun onUpdateBaseUrlClicked(source: ParsedNiddlerMessage?)
    }

}

/**
 * @author Nicola Verbeeck
 */
open class NiddlerStructuredViewPopupMenu(val listener: Listener) : JPopupMenu() {

    private val copyValueItem: JMenuItem = JMenuItem("Copy value")
    private val copyKeyItem: JMenuItem = JMenuItem("Copy key")
    private var key: Any? = null
    private var value: Any? = null

    init {
        copyValueItem.apply {
            addActionListener { value?.let { listener.onCopyValueClicked(it) } }
        }
        copyKeyItem.apply {
            addActionListener { key?.let { listener.onCopyKeyClicked(it) } }
        }
    }

    final override fun add(comp: JMenuItem?): JMenuItem {
        return super.add(comp)
    }

    fun init(key: Any?, value: Any?) {
        this.key = key
        this.value = value

        remove(copyKeyItem)
        if (key != null)
            add(copyKeyItem)

        remove(copyValueItem)
        if (value != null)
            add(copyValueItem)
    }

    interface Listener {
        fun onCopyKeyClicked(key: Any)
        fun onCopyValueClicked(value: Any)
    }

}