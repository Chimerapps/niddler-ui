package com.icapps.niddler.lib.connection

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener

/**
 * @author nicolaverbeeck
 */
open class NiddlerMessageListenerAdapter : NiddlerMessageListener {

    override fun onServerInfo(serverInfo: NiddlerServerInfo) {
    }

    override fun onAuthRequest(): String? {
        return null
    }

    override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
    }

    override fun onReady() {
    }

    override fun onDebuggerAttached() {
    }

    override fun onDebuggerActive() {
    }

    override fun onDebuggerInactive() {
    }

    override fun onClosed() {
    }

}