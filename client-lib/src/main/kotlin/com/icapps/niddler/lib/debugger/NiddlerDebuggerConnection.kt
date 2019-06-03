package com.icapps.niddler.lib.debugger

/**
 * @author Nicola Verbeeck
 */
interface NiddlerDebuggerConnection {

    fun sendMessage(message: String)

}