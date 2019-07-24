package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class NiddlerStatusBar : JPanel(BorderLayout()), NiddlerMessageListener {

    private val statusText = JBLabel().apply {
        isFocusable = false
        text = ""
        verifyInputWhenFocusTarget = false
    }

    private var status: Status = Status.DISCONNECTED
    private var serverInfo: NiddlerServerInfo? = null

    init {
        add(statusText, BorderLayout.CENTER)
        border = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null), EmptyBorder(1, 6, 1, 6))
        updateStatusText()
        updateStatusIcon()
    }

    override fun onServerInfo(serverInfo: NiddlerServerInfo) {
        this.serverInfo = serverInfo
        updateStatusText()
    }

    override fun onReady() {
        status = Status.CONNECTED
        updateStatusIcon()
    }

    override fun onClosed() {
        status = Status.DISCONNECTED
        serverInfo = null
        updateStatusIcon()
        updateStatusText()
    }

    private fun updateStatusText() {
        val text = when (status) {
            Status.CONNECTED -> buildText("Connected", "to")
            Status.DISCONNECTED -> "Disconnected"
        }
        statusText.text = text
    }

    private fun updateStatusIcon() {
        statusText.icon = when (status) {
            Status.CONNECTED -> IncludedIcons.Status.connected
            Status.DISCONNECTED -> IncludedIcons.Status.disconnected
        }
    }

    @Suppress("SameParameterValue")
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
        CONNECTED, DISCONNECTED
    }

}