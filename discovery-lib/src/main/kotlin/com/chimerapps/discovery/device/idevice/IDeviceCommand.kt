package com.chimerapps.discovery.device.idevice

import com.chimerapps.discovery.utils.IDeviceCommandExecutor

internal interface IDeviceCommand<T> {

    val command: String
    val arguments: List<String>

    fun onProcessStarted(process: Process, onFinishListener: (code: Int) -> Unit) {
        onFinishListener(try {
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            -1
        })
    }

    fun handle(commandExecutor: IDeviceCommandExecutor, resultCode: Int, stdOut: List<String>, stdErr: List<String>): T

}