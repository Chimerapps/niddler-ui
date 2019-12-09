package com.chimerapps.discovery.device.idevice

import com.chimerapps.discovery.utils.IDeviceCommandExecutor

internal class IProxyDeviceCommand(udid: String, localPort: Int, targetPort: Int, private val readyListener: () -> Unit) : IDeviceCommand<Int> {

    override val command: String = "iproxy"
    override val arguments: List<String> = listOf(localPort.toString(), targetPort.toString(), udid)

    override fun onProcessStarted(process: Process, onFinishListener: (code: Int) -> Unit) {
        //Don't wait for process to finish, just notify if we can read 1 byte -> ready
        if (process.inputStream.read() != -1)
            readyListener()
        onFinishListener(try {
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            -1
        })
    }

    override fun handle(commandExecutor: IDeviceCommandExecutor, resultCode: Int, stdOut: List<String>, stdErr: List<String>): Int {
        return 1
    }
}