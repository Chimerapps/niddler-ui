package com.icapps.niddler.ui.form.debug.impl

import com.icapps.niddler.ui.form.debug.DebugToolbar
import com.icapps.niddler.ui.util.simpleAction
import javax.swing.JButton
import javax.swing.JPopupMenu
import javax.swing.JToolBar

/**
 * @author nicolaverbeeck
 */
class SwingDebugToolbar : DebugToolbar, JToolBar(JToolBar.HORIZONTAL) {

    override var listener: DebugToolbar.DebugToolbarListener? = null
    private val menu: JPopupMenu = JPopupMenu()
    private lateinit var addButton: JButton
    private val removeButton: JButton
    private val configureDelaysButton: JButton

    init {
        isFloatable = false
        menu.add(simpleAction("Add blacklist") {
            listener?.onAddBlacklist()
        })
        menu.add(simpleAction("Add request intercept") {
            listener?.onAddRequestInterceptor()
        })
        menu.add(simpleAction("Add request override") {
            listener?.onAddRequestOverride()
        })
        menu.add(simpleAction("Add response override") {
            listener?.onAddResponseOverride()
        })

        addButton = add(simpleAction("Add", "/add.png") {
            menu.show(addButton, addButton.x, addButton.y)
        })
        removeButton = add(simpleAction("Remove", "/remove.png") {
            listener?.onRemoveClicked()
        })
        configureDelaysButton = add(simpleAction("Configure delays", "/clock.png") {
            listener?.onConfigureDelays()
        })
    }

    override fun setRemoveEnabled(enabled: Boolean) {
        removeButton.isEnabled = enabled
    }
}