package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.debugger.DebuggingSession
import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration

/**
 * @author nicolaverbeeck
 */
class ConcreteDebuggingSession(private val debuggerInterface: DebuggerInterface) : DebuggingSession {

    private var sessionActive: Boolean = false

    override val isActive: Boolean
        get() = sessionActive

    private var currentConfiguration: DebuggerConfiguration? = null
    private var configurationSent = false

    override fun startSession() {
        sessionActive = true
        debuggerInterface.activate()
        if (!configurationSent) {
            currentConfiguration?.let { applyConfiguration(it) }
        }
    }

    override fun stopSession() {
        sessionActive = false
        debuggerInterface.deactivate()
    }

    override fun applyConfiguration(debuggerConfiguration: DebuggerConfiguration) {
        currentConfiguration = debuggerConfiguration

        if (sessionActive) {
            DebuggerConfigurationBridge(debuggerConfiguration, debuggerInterface).apply()
            configurationSent = true
        } else {
            configurationSent = false
        }
    }

}