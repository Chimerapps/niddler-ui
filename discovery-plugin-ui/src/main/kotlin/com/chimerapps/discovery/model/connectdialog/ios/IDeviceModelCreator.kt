package com.chimerapps.discovery.model.connectdialog.ios

import com.chimerapps.discovery.device.idevice.IDevice
import com.chimerapps.discovery.model.connectdialog.DeviceModel
import com.chimerapps.discovery.ui.IncludedLibIcons
import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn

/**
 * @author Nicola Verbeeck
 */
class IDeviceModelCreator(
    private val announcementPort: Int,
) {

    fun buildDeviceModel(iDevice: IDevice): DeviceModel? {
        return try {
            val name = iDevice.deviceInfo.deviceName
            val extraInfo = "(${iDevice.deviceInfo.deviceType?.simpleName ?: iDevice.deviceInfo.alternativeDeviceTypeName}, ${iDevice.deviceInfo.osVersion})"

            val sessions = iDevice.getSessions(announcementPort)
            DeviceModel(
                name, extraInfo, IncludedLibIcons.Devices.realApple,
                iDevice.deviceInfo.udid, iDevice, sessions
            )
        } catch (e: Throwable) {
            logger<IDeviceModelCreator>().warn("Failed to build device model:", e)
            null
        }
    }

}
