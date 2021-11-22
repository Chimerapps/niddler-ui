package com.chimerapps.discovery.device.idevice

import com.chimerapps.discovery.utils.IDeviceCommandExecutor

internal class IDeviceInfoCommand(private val udid: String) : IDeviceCommand<IDeviceInfo?> {

    private val regex = Regex("([^:]+):(.+)")

    override val command: String = "ideviceinfo"
    override val arguments: List<String> = listOf("-u", udid)

    override fun handle(commandExecutor: IDeviceCommandExecutor, resultCode: Int, stdOut: List<String>, stdErr: List<String>): IDeviceInfo? {
        if (resultCode != 0)
            return null

        var wifiAddress: String? = null
        var deviceName: String? = null
        var productType: String? = null
        var productVersion: String? = null
        stdOut.forEach { line ->
            regex.matchEntire(line)?.destructured?.let { (key, value) ->
                val trimmedValue = value.trim()
                when (key) {
                    "WiFiAddress" -> wifiAddress = trimmedValue
                    "DeviceName" -> deviceName = trimmedValue
                    "ProductType" -> productType = trimmedValue
                    "ProductVersion" -> productVersion = trimmedValue
                }
            }
        }

        return IDeviceInfoImpl(
            udid = udid,
            wifiMacAddress = wifiAddress ?: return null,
            deviceName = deviceName ?: return null,
            deviceType = IDeviceType.values().find { it.productType == productType },
            osVersion = productVersion ?: return null,
            alternativeDeviceTypeName = productType ?: return null,
        )
    }
}