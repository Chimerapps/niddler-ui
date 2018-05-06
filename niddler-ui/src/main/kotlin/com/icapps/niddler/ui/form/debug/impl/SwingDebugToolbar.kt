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

    init {
        isFloatable = false
        menu.add(simpleAction<SwingDebugToolbar>("Add blacklist") {
            listener?.onAddBlacklist()
        })
        menu.add(simpleAction<SwingDebugToolbar>("Add request override") {
            listener?.onAddRequestOverride()
        })
        menu.add(simpleAction<SwingDebugToolbar>("Add request intercept") {
            listener?.onAddResponseOverride()
        })
        menu.add(simpleAction<SwingDebugToolbar>("Add response intercept") {
            listener?.addResponseInterceptor()
        })

        addButton = add(simpleAction<SwingDebugToolbar>("Add", "/add.png") {
            menu.show(addButton, addButton.x, addButton.y)
        })
        removeButton = add(simpleAction<SwingDebugToolbar>("Remove", "/remove.png") {
            listener?.onRemoveClicked()
        })
    }

    override fun setRemoveEnabled(enabled: Boolean) {
        removeButton.isEnabled = enabled
    }
}