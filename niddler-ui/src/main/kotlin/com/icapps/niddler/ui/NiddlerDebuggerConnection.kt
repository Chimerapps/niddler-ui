package com.icapps.niddler.ui

import com.icapps.niddler.lib.debugger.NiddlerDebuggerConnection

/**
 * @author nicolaverbeeck
 */
class NiddlerClientDebuggerInterface(private val niddlerClient: NiddlerClient) : NiddlerDebuggerConnection {

    override fun sendMessage(message: String) {
        niddlerClient.send(message)
    }
}