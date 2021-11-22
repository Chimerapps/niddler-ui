package com.chimerapps.discovery.device.idevice

import com.chimerapps.discovery.device.BaseDevice
import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.PreparedDeviceConnection
import com.chimerapps.discovery.utils.IDeviceCommandExecutor
import com.chimerapps.discovery.utils.Platform
import com.chimerapps.discovery.utils.currentPlatform
import com.chimerapps.discovery.utils.freePort
import java.io.File
import java.util.concurrent.CountDownLatch

interface IDeviceInfo {

    val udid: String
    val wifiMacAddress: String
    val deviceName: String
    val deviceType: IDeviceType?
    val alternativeDeviceTypeName: String?
    val osVersion: String
}

class IDeviceBootstrap(private val binaryPath: File = File("/usr/local/bin/")) {

    private val executor = IDeviceCommandExecutor(binaryPath)

    val devices: List<IDevice>
        get() {
            if (currentPlatform != Platform.DARWIN)
                return emptyList()

            return executor.execute(ListDevicesCommand()).get().map { IDevice(it, executor) }
        }

    val isRealConnection: Boolean
        get() = File(binaryPath, "idevice_id").let { it.exists() && it.canExecute() }
}

internal data class IDeviceInfoImpl(override val udid: String,
                                    override val wifiMacAddress: String,
                                    override val deviceName: String,
                                    override val deviceType: IDeviceType?,
                                    override val osVersion: String,
                                    override val alternativeDeviceTypeName: String?) : IDeviceInfo

class IDevice internal constructor(val deviceInfo: IDeviceInfo,
                                   private val commandExecutor: IDeviceCommandExecutor) : BaseDevice() {

    private fun forwardTCPPort(localPort: Int, remotePort: Int): Thread {
        val countDownLatch = CountDownLatch(1)
        val thread = Thread {
            commandExecutor.execute(IProxyDeviceCommand(deviceInfo.udid, localPort, targetPort = remotePort) {
                countDownLatch.countDown()
            })
        }.also {
            it.start()
        }
        countDownLatch.await()
        return thread
    }

    override fun getSessions(announcementPort: Int): List<DiscoveredSession> {
        val freePort = freePort()
        if (freePort <= 0) {
            return emptyList()
        }
        var thread: Thread? = null
        try {
            thread = forwardTCPPort(freePort, announcementPort)
            return readAnnouncement(freePort)
        } finally {
            //This should kill the process and release the port
            thread?.interrupt()
            Thread.interrupted() //Clear flag
            thread?.join()
        }
    }

    override fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection {
        val thread = forwardTCPPort(suggestedLocalPort, remotePort)
        return object : DirectPreparedConnection("127.0.0.1", suggestedLocalPort) {
            override fun tearDown() {
                try {
                    super.tearDown()
                } catch (ignore: Throwable) {
                }

                //This should kill the process and release the port
                thread.interrupt()
                Thread.interrupted() //Clear flag
                thread.join()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IDevice

        if (deviceInfo.udid != other.deviceInfo.udid) return false

        return true
    }

    override fun hashCode(): Int {
        return deviceInfo.udid.hashCode()
    }


}