package com.chimerapps.discovery.device.idevice

import com.chimerapps.discovery.utils.IDeviceCommandExecutor
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

internal class IProxyDeviceCommand(udid: String, private val localPort: Int, targetPort: Int, private val readyListener: () -> Unit) : IDeviceCommand<Int> {

    private companion object {
        private val MAX_TIME_WAITING = TimeUnit.MILLISECONDS.toNanos(1500L)
    }

    override val command: String = "iproxy"
    override val arguments: List<String> = listOf(localPort.toString(), targetPort.toString(), udid)

    override fun onProcessStarted(process: Process, onFinishListener: (code: Int) -> Unit) {
        //Don't wait for process to finish, just notify if we can connect on the local port

        val start = System.nanoTime()
        var ok = false
        while ((System.nanoTime() - start) < MAX_TIME_WAITING) {
            if (Thread.currentThread().isInterrupted) {
                return
            }

            try {
                Socket(InetAddress.getLoopbackAddress(), localPort).close()
                ok = true
                break
            } catch (e: InterruptedException) {
                try {
                    process.destroy()
                } catch (e: Throwable) {
                }
                throw e
            } catch (e: Throwable) {
                if (Thread.currentThread().isInterrupted) {
                    return
                }
            }
        }
        if (ok) {
            readyListener()
        } else {
            try {
                process.destroy()
            } catch (e: Throwable) {
            }
        }
        super.onProcessStarted(process, onFinishListener)
    }

    override fun handle(commandExecutor: IDeviceCommandExecutor, resultCode: Int, stdOut: List<String>, stdErr: List<String>): Int {
        return 1
    }
}