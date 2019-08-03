package com.chimerapps.discovery.device.local

import com.chimerapps.discovery.device.BaseDevice
import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.PreparedDeviceConnection

/**
 * @author Nicola Verbeeck
 */
class LocalDevice : BaseDevice() {

    override fun getSessions(announcementPort: Int): List<DiscoveredSession> {
        return readAnnouncement(announcementPort)
    }

    override fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection {
        return DirectPreparedConnection("127.0.0.1", remotePort)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        return (other is LocalDevice)
    }

    override fun hashCode(): Int {
        return "local".hashCode()
    }
}