package com.chimerapps.discovery.device.idevice

import com.chimerapps.discovery.utils.IDeviceCommandExecutor

internal class ListDevicesCommand : IDeviceCommand<List<IDeviceInfo>> {

    override val command: String = "idevice_id"
    override val arguments: List<String> = listOf("-l")

    override fun handle(commandExecutor: IDeviceCommandExecutor, resultCode: Int, stdOut: List<String>, stdErr: List<String>): List<IDeviceInfo> {
        if (resultCode == 0) {
            return stdOut.mapNotNull {
                commandExecutor.execute(IDeviceInfoCommand(it.trim())).get()
            }
        }
        return emptyList()
    }
}