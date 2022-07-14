package com.chimerapps.discovery.model.connectdialog.local

import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.local.LocalDevice
import com.chimerapps.discovery.model.connectdialog.DeviceModel
import com.chimerapps.discovery.ui.IncludedLibIcons
import com.chimerapps.discovery.utils.localName

/**
 * @author Nicola Verbeeck
 */
class LocalDeviceModelCreator {

    fun buildDeviceModel(localDevice: LocalDevice, niddlerSessions: List<DiscoveredSession>): DeviceModel {
        return DeviceModel(
            localName, "", IncludedLibIcons.Devices.computer,
            "local", localDevice, niddlerSessions
        )
    }

}