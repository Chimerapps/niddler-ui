package com.chimerapps.discovery.device.debugbridge

import com.chimerapps.discovery.device.BaseDevice
import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.PreparedDeviceConnection
import com.chimerapps.discovery.device.sdb.SDBDevice
import com.chimerapps.discovery.utils.freePort
import se.vidstige.jadb.JadbDevice

abstract class DebugBridgeBasedDevice(device: JadbDevice, val bootstrap: DebugBridgeBootstrap) : BaseDevice() {

    val serial: String = device.serial.trim()

    fun forwardTCPPort(localPort: Int, remotePort: Int) {
        bootstrap.executeDebugBridgeCommand("-s", serial, "forward", "tcp:$localPort", "tcp:$remotePort")
    }

    fun removeTCPForward(localPort: Int) {
        bootstrap.executeDebugBridgeCommand("-s", serial, "forward", "--remove", "tcp:$localPort")
    }

    fun executeDebugBridgeCommand(vararg args: String): String? {
        val newArgs = Array(args.size + 2) { "" }
        newArgs[0] = "-s"
        newArgs[1] = serial
        System.arraycopy(args, 0, newArgs, 2, args.size)
        return bootstrap.executeDebugBridgeCommand(*newArgs)
    }

    fun executeDebugBridgeCommand(timeoutInSeconds: Long, vararg args: String): String? {
        val newArgs = Array(args.size + 2) { "" }
        newArgs[0] = "-s"
        newArgs[1] = serial
        System.arraycopy(args, 0, newArgs, 2, args.size)
        return bootstrap.executeDebugBridgeCommand(timeoutInSeconds, *newArgs)
    }

    override fun getSessions(announcementPort: Int): List<DiscoveredSession> {
        val freePort = freePort()
        if (freePort <= 0) {
            return emptyList()
        }
        try {
            forwardTCPPort(freePort, announcementPort)
            return readAnnouncement(freePort)
        } finally {
            removeTCPForward(freePort)
        }
    }

    override fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection {
        forwardTCPPort(suggestedLocalPort, remotePort)
        return object : DirectPreparedConnection("127.0.0.1", suggestedLocalPort) {
            override fun tearDown() {
                try {
                    super.tearDown()
                } catch (ignore: Throwable) {
                }
                removeTCPForward(suggestedLocalPort)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SDBDevice

        if (serial != other.serial) return false

        return true
    }

    override fun hashCode(): Int {
        return serial.hashCode()
    }

}