package com.icapps.niddler.lib.device.local

import com.icapps.niddler.lib.device.BaseDevice
import com.icapps.niddler.lib.device.Device
import com.icapps.niddler.lib.device.DirectPreparedConnection
import com.icapps.niddler.lib.device.NiddlerSession
import com.icapps.niddler.lib.device.PreparedDeviceConnection

/**
 * @author nicolaverbeeck
 */
class LocalDevice : BaseDevice() {

    override fun getNiddlerSessions(): List<NiddlerSession> {
        return readAnnouncement(Device.ANNOUNCEMENT_PORT)
    }

    override fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection {
        return DirectPreparedConnection("127.0.0.1", remotePort)
    }

}