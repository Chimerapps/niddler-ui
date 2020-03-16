package com.chimerapps.niddler.ui.util.session

import com.chimerapps.discovery.device.AnnouncementExtension
import com.chimerapps.discovery.device.Device
import com.chimerapps.discovery.device.adb.ADBInterface
import com.chimerapps.discovery.device.idevice.IDeviceBootstrap
import com.chimerapps.discovery.device.local.LocalDevice
import com.chimerapps.discovery.ui.DiscoveredDeviceConnection

class SessionFinderUtil(private val adbInterface: ADBInterface?,
                        private val iDeviceBootstrap: IDeviceBootstrap,
                        private val port: Int) {

    fun findSessionWithTag(tag: String): DiscoveredDeviceConnection? {
        findSessionOnDevice(LocalDevice(), tag)?.let { return it }
        adbInterface?.devices?.forEach { device -> findSessionOnDevice(device, tag)?.let { return it } }
        iDeviceBootstrap.devices.forEach { device -> findSessionOnDevice(device, tag)?.let { return it } }

        return null
    }

    fun findSessionOnDevice(device: Device, tag: String): DiscoveredDeviceConnection? {
        val session = device.getSessions(port).find { session ->
            session.extensions?.any { it.name == AnnouncementExtension.EXTENSION_NAME_TAG && it.decodeAsString == tag } == true
        } ?: return null

        return DiscoveredDeviceConnection(device, session)
    }

}