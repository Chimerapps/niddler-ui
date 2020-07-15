package com.chimerapps.niddler.ui.debugging.breakpoints

import com.chimerapps.niddler.ui.debugging.rewrite.location.EditLocationUI
import com.chimerapps.niddler.ui.util.ext.trimToNull
import com.chimerapps.niddler.ui.util.ui.addChangeListener
import com.chimerapps.niddler.ui.util.ui.dispatchMain
import com.icapps.niddler.lib.debugger.model.breakpoint.Breakpoint
import com.icapps.niddler.lib.debugger.model.configuration.DebugLocation
import java.awt.BorderLayout
import java.awt.Window
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.reflect.KProperty1

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

        contentUi.host.addChangeListener {
            dispatchTextUpdateIfRequired(it, DebugLocation::host) { location, newValue ->
                location.copy(host = newValue)
            }
        }
        contentUi.port.addChangeListener {
            dispatchTextUpdateIfRequired(it, DebugLocation::port) { location, newValue ->
                location.copy(port = newValue)
            }
        }
        contentUi.path.addChangeListener {
            dispatchTextUpdateIfRequired(it, DebugLocation::path) { location, newValue ->
                location.copy(path = newValue)
            }
        }
        contentUi.query.addChangeListener {
            dispatchTextUpdateIfRequired(it, DebugLocation::query) { location, newValue ->
                location.copy(query = newValue)
            }
        }
        contentUi.protocolChooser.editor.editorComponent.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                dispatchMain {
                    dispatchTextUpdateIfRequired(contentUi.protocolChooser.editor.item?.toString()?.trimToNull(), DebugLocation::protocol) { location, newValue ->
                        location.copy(protocol = newValue)
                    }
                }
            }
        })
        contentUi.protocolChooser.addActionListener {
            dispatchTextUpdateIfRequired(contentUi.protocolChooser.selectedItem?.toString()?.trimToNull(), DebugLocation::protocol) { location, newValue ->
                location.copy(protocol = newValue)
            }
        }
        contentUi.actionChooser?.editor?.editorComponent?.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                dispatchMain {
                    dispatchBreakpointTextUpdateIfRequired(contentUi.actionChooser.editor?.item?.toString()?.trimToNull(), Breakpoint::method) { breakpoint, newValue ->
                        breakpoint.copy(method = newValue)
                    }
                }
            }
        })
        contentUi.actionChooser?.addActionListener {
            dispatchBreakpointTextUpdateIfRequired(contentUi.actionChooser.selectedItem?.toString()?.trimToNull(), Breakpoint::method) { breakpoint, newValue ->
                breakpoint.copy(method = newValue)
            }
        }
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

    private fun dispatchTextUpdateIfRequired(field: JTextField, selector: KProperty1<DebugLocation, String?>, creator: (DebugLocation, String?) -> DebugLocation) {
        dispatchTextUpdateIfRequired(field.text.trimToNull(), selector, creator)
    }

    private fun dispatchTextUpdateIfRequired(newValue: String?, selector: KProperty1<DebugLocation, String?>, creator: (DebugLocation, String?) -> DebugLocation) {
        val item = _currentItemInternal ?: return
        val location = item.location
        if (selector.get(location.location) == newValue) return

        val newLocation = location.copy(location = creator(location.location, newValue))
        val locationString = newLocation.location.asString()
        val updatedItem = item.copy(locations = listOf(newLocation), name = if (item.method.isNullOrEmpty()) locationString else "${item.method} $locationString")
        _currentItemInternal = updatedItem
        onItemUpdated(item, updatedItem)
    }

    private fun dispatchBreakpointTextUpdateIfRequired(newValue: String?, selector: KProperty1<Breakpoint, String?>, creator: (Breakpoint, String?) -> Breakpoint) {
        val item = _currentItemInternal ?: return
        if (selector.get(item) == newValue) return

        val updatedItem = creator(item, newValue)
        val locationString = updatedItem.location.location.asString()
        val updatedItemWithName = updatedItem.copy(name = if (updatedItem.method.isNullOrEmpty()) locationString else "${updatedItem.method} $locationString")

        _currentItemInternal = updatedItemWithName
        onItemUpdated(item, updatedItemWithName)
    }
}