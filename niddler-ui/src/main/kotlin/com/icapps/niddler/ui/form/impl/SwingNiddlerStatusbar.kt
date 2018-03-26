package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.form.ui.NiddlerStatusbar
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import com.icapps.niddler.ui.util.loadIcon
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * @author nicolaverbeeck
 */
class SwingNiddlerStatusbar : NiddlerStatusbar {

    private var status: Status = Status.DISCONNECTED
    private var serverInfo: NiddlerServerInfo? = null

    val statusBar: JPanel = JPanel()
    private val statusText: JLabel

    init {
        statusBar.layout = BorderLayout(0, 0)
        statusBar.border = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null)
        statusText = JLabel().apply {
            isFocusable = false
            text = ""
            verifyInputWhenFocusTarget = false
            putClientProperty("html.disable", java.lang.Boolean.FALSE)
        }
        statusBar.add(statusText, BorderLayout.CENTER)
        statusBar.border = BorderFactory.createCompoundBorder(statusBar.border, EmptyBorder(1, 6, 1, 6))

        onDisconnected()
    }

    override fun onDebuggerAttached() {
        status = Status.DEBUGGER_CONNECTED
        updateStatusText()
        updateStatusIcon()
    }

    override fun onDebuggerStatusChanged(active: Boolean) {
        status = if (active) Status.DEBUGGER_ACTIVE else Status.DEBUGGER_INACTIVE
        updateStatusText()
        updateStatusIcon()
    }

    override fun onConnected() {
        status = Status.CONNECTED
        updateStatusText()
        updateStatusIcon()
    }

    override fun onDisconnected() {
        status = Status.DISCONNECTED
        updateStatusText()
        updateStatusIcon()
    }

    override fun onApplicationInfo(information: NiddlerServerInfo) {
        serverInfo = information
        updateStatusText()
    }

    private fun updateStatusText() {
        val text = when (status) {
            SwingNiddlerStatusbar.Status.CONNECTED -> buildText("Connected", "to")
            SwingNiddlerStatusbar.Status.DISCONNECTED -> "Disconnected"
            SwingNiddlerStatusbar.Status.DEBUGGER_CONNECTED -> buildText("Debugger connected", "on")
            SwingNiddlerStatusbar.Status.DEBUGGER_ACTIVE -> buildText("Debugger active", "on")
            SwingNiddlerStatusbar.Status.DEBUGGER_INACTIVE -> buildText("Debugger inactive", "on")
        }
        statusText.text = text
    }

    private fun updateStatusIcon() {
        val resourceString = when (status) {
            SwingNiddlerStatusbar.Status.CONNECTED -> "/ic_connected.png"
            SwingNiddlerStatusbar.Status.DISCONNECTED -> "/ic_disconnected.png"
            SwingNiddlerStatusbar.Status.DEBUGGER_CONNECTED -> "/ic_debug_connected.png"
            SwingNiddlerStatusbar.Status.DEBUGGER_ACTIVE -> "/ic_debug_active.png"
            SwingNiddlerStatusbar.Status.DEBUGGER_INACTIVE -> "/ic_debug_inactive.png"
        }
        statusText.icon = resourceString.loadIcon<SwingNiddlerStatusbar>()
    }

    private fun buildText(prefix: String, glue: String): String {
        val builder = StringBuilder()
        builder.append(prefix)
        serverInfo?.let {
            builder.append(' ').append(glue).append(' ')
                    .append(it.serverName)
                    .append(" (").append(it.serverDescription).append(") - V")
                    .append(it.protocol)
        }
        return builder.toString()
    }

    private enum class Status {
        CONNECTED, DISCONNECTED, DEBUGGER_CONNECTED, DEBUGGER_ACTIVE, DEBUGGER_INACTIVE
    }

}