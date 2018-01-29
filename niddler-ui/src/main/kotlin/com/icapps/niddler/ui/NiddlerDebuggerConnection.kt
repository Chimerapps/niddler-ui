package com.icapps.niddler.ui

/**
 * @author nicolaverbeeck
 */
interface NiddlerDebuggerConnection {

    fun sendMessage(message: String)

}