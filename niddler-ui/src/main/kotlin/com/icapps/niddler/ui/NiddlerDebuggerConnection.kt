package com.icapps.niddler.ui

/**
 * @author nicolaverbeeck
 */
interface NiddlerDebuggerConnection {

    fun sendMessage(message: String)

}

class NiddlerClientDebuggerInterface(private val niddlerClient: NiddlerClient) : NiddlerDebuggerConnection {

    override fun sendMessage(message: String) {
        niddlerClient.send(message)
    }
}