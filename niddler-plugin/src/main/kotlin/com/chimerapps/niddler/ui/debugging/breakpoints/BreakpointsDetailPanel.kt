package com.chimerapps.niddler.ui.debugging.breakpoints

import com.chimerapps.niddler.ui.debugging.rewrite.location.EditLocationUI
import com.icapps.niddler.lib.debugger.model.breakpoint.Breakpoint
import java.awt.BorderLayout
import java.awt.Window
import javax.swing.JPanel

@Suppress("DuplicatedCode")
class BreakpointsDetailPanel(private val parentWindow: Window,
                             private val onItemUpdated: (old: Breakpoint, new: Breakpoint) -> Unit) : JPanel(BorderLayout()) {

    private var _currentItemInternal: Breakpoint? = null

    var currentItem: Breakpoint?
        set(value) {
            if (value == _currentItemInternal) return
            _currentItemInternal = value
            updateContents()
        }
        get() = _currentItemInternal

    private val contentUi = EditLocationUI(includeAction = true, includeButtons = false)

    init {
        add(contentUi.content, BorderLayout.NORTH)
    }

    private fun updateContents() {
        val location = _currentItemInternal?.locations?.first()?.location
        contentUi.host.text = location?.host ?: ""
        contentUi.port.text = location?.port ?: ""
        contentUi.path.text = location?.path ?: ""
        contentUi.query.text = location?.query ?: ""
        contentUi.protocolChooser.selectedItem = location?.protocol ?: ""
        contentUi.actionChooser?.selectedItem = _currentItemInternal?.method ?: ""
    }
}