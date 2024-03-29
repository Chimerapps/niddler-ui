package com.chimerapps.niddler.ui.util.execution.tool

import com.android.ddmlib.IDevice
import com.android.tools.idea.run.AndroidProcessHandler
import com.android.tools.idea.run.deployment.AndroidExecutionTarget
import com.chimerapps.discovery.device.Device
import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.DiscoveredSession
import com.chimerapps.discovery.device.PreparedDeviceConnection
import com.chimerapps.niddler.ui.QuickConnectionInfo
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import java.lang.reflect.Method

class AndroidQuickConnectHelper(
    private val executionEnvironment: ExecutionEnvironment,
    private val handler: ProcessHandler,
    private val project: Project
) {

    companion object {

        var multiDeviceCall: Method? = null
            private set
        var multiDevice2Call: Method? = null
            private set
        var singleDeviceCall: Method? = null
            private set
        var getClientDataCall: Method? = null
            private set
        var getPidCall: Method? = null
            private set

        val isAndroidSupported = try {
            val target = Class.forName("com.android.tools.idea.run.deployment.AndroidExecutionTarget")
            multiDeviceCall = target.declaredMethods.find { it.name == "getDevices" }?.also { it.isAccessible = true }
            singleDeviceCall = target.declaredMethods.find { it.name == "getIDevice" }?.also { it.isAccessible = true }
            multiDevice2Call = target.declaredMethods.find { it.name == "getRunningDevices" }?.also { it.isAccessible = true }

            Class.forName("com.android.tools.idea.run.AndroidProcessHandler")
            multiDeviceCall != null || singleDeviceCall != null || multiDevice2Call != null
        } catch (e: Throwable) {
            false
        }

        val hasClient = try {
            val client = Class.forName("com.android.ddmlib.Client")
            val clientData = Class.forName("com.android.ddmlib.ClientData")
            getClientDataCall = client.declaredMethods.find { it.name == "getClientData" }?.also { it.isAccessible = true }
            getPidCall = clientData.declaredMethods.find { it.name == "getPid" }?.also { it.isAccessible = true }

            getClientDataCall != null && getPidCall != null
        } catch (e: Throwable) {
            false
        }
    }

    fun getQuickConnectionInfo(port: Int, tag: String): QuickConnectionInfo? {
        if (!isAndroidSupported) {
            return null
        }

        try {
            if (handler !is AndroidProcessHandler) {
                return null
            }
            val target = (executionEnvironment.executionTarget as? AndroidExecutionTarget) ?: return null

            return findDevice(target)?.let {
                return QuickConnectionInfo(port, tag, IDeviceWrapper(it))
            }
        } catch (e: Throwable) {
            return null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun findDevice(target: AndroidExecutionTarget): IDevice? {
        handler as AndroidProcessHandler

        (multiDeviceCall?.invoke(target) as? Collection<IDevice>)?.let { devices ->
            devices.forEach { device ->
                if (handler.isAssociated(device)) {
                    return device
                }
            }
        }
        (multiDevice2Call?.invoke(target) as? Collection<IDevice>)?.let { devices ->
            devices.forEach { device ->
                if (handler.isAssociated(device)) {
                    return device
                }
            }
        }
        (singleDeviceCall?.invoke(target) as? IDevice)?.let { device ->
            if (handler.isAssociated(device))
                return device
        }
        return null
    }

    fun isSupported(): Boolean = isAndroidSupported && handler is AndroidProcessHandler

    fun getDeviceProcessInfo(): DeviceWithProcessId? {
        if (!isAndroidSupported || !hasClient) {
            return null
        }
        try {
            if (handler !is AndroidProcessHandler) {
                return null
            }
            val target = (executionEnvironment.executionTarget as? AndroidExecutionTarget) ?: return null

            return findDevice(target)?.let { device ->
                val client = handler.getClient(device) ?: return@let null
                val clientData = getClientDataCall!!.invoke(client)
                val pid = getPidCall!!.invoke(clientData) as Int

                return DeviceWithProcessId(device.serialNumber, pid)
            }
        } catch (e: Throwable) {
            return null
        }
    }

}

private class IDeviceWrapper(private val iDevice: IDevice) : Device {

    private val removeForwardMethod: Method? = iDevice.javaClass.declaredMethods.find { it.name == "removeForward" }?.also { it.isAccessible = true }

    override fun getSessions(announcementPort: Int): List<DiscoveredSession> {
        throw IllegalStateException("Not supported")
    }

    override fun prepareConnection(suggestedLocalPort: Int, remotePort: Int): PreparedDeviceConnection {
        iDevice.createForward(suggestedLocalPort, remotePort)
        return object : DirectPreparedConnection("127.0.0.1", suggestedLocalPort) {
            override fun tearDown() {
                try {
                    if (removeForwardMethod?.parameterCount == 1) {
                        removeForwardMethod.invoke(iDevice, suggestedLocalPort)
                    } else if (removeForwardMethod?.parameterCount == 2) {
                        removeForwardMethod.invoke(iDevice, suggestedLocalPort, remotePort)
                    }
                } catch (ignore: Throwable) {
                }
            }
        }
    }
}

data class DeviceWithProcessId(
    val deviceSerial: String,
    val processId: Int,
)