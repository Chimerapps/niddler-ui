package com.icapps.niddler.ui.debugger

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration

/**
 * @author nicolaverbeeck
 */
interface DebuggingSession {

    val isActive: Boolean

    fun startSession()

    fun stopSession()

    fun applyConfiguration(debuggerConfiguration: DebuggerConfiguration)

}