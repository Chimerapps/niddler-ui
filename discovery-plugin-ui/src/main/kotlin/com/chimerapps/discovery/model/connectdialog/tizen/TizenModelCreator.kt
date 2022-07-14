package com.chimerapps.discovery.model.connectdialog.tizen

import com.chimerapps.discovery.device.debugbridge.DebugBridgeBasedDevice
import com.chimerapps.discovery.model.connectdialog.DeviceModel
import com.chimerapps.discovery.ui.IncludedLibIcons
import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn

/**
 * @author Nicola Verbeeck
 */
class TizenModelCreator(
    private val announcementPort: Int,
) {

    fun buildDeviceModel(device: DebugBridgeBasedDevice): DeviceModel? {
        return try {
            val name = device.serial.trim()

            val sessions = device.getSessions(announcementPort)
            DeviceModel(
                name, "", IncludedLibIcons.Devices.realTizen,
                name, device, sessions,
            )
        } catch (e: Throwable) {
            logger<TizenModelCreator>().warn("Failed to build device model:", e)
            null
        }
    }

}