package com.icapps.niddler.lib.debugger

import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration

/**
 * @author Nicola Verbeeck
 */
interface DebuggingSession {

    val isActive: Boolean

    fun startSession()

    fun stopSession()

    fun applyConfiguration(debuggerConfiguration: DebuggerConfiguration)

}