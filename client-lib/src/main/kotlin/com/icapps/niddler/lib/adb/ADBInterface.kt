package com.icapps.niddler.lib.adb

import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice

/**
 * @author nicolaverbeeck
 */
class ADBInterface(private val bootstrap: ADBBootstrap, private val connection: JadbConnection? = null) {

    val devices: List<ADBDevice>
        get() = connection?.let {
            connection.devices.map {
                ADBDevice(it, bootstrap)
            }
        } ?: emptyList()

}

class ADBDevice(private val device: JadbDevice, private val bootstrap: ADBBootstrap) {

    val serial = device.serial

    fun forwardTCPPort(localPort: Int, remotePort: Int) {
        val serial = device.serial
        if (serial != null)
            bootstrap.executeADBCommand("-s", serial, "forward", "tcp:$localPort", "tcp:$remotePort")
        else
            bootstrap.executeADBCommand("forward", "tcp:$localPort", "tcp:$remotePort")
    }

}