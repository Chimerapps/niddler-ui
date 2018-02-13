package com.icapps.niddler.ui.form.ui

import com.icapps.niddler.ui.model.messages.NiddlerServerInfo

/**
 * @author nicolaverbeeck
 */
interface NiddlerStatusbar {

    fun onDebuggerAttached()

    fun onDebuggerStatusChanged(active: Boolean)

    fun onConnected()

    fun onDisconnected()

    fun onApplicationInfo(information: NiddlerServerInfo)

}