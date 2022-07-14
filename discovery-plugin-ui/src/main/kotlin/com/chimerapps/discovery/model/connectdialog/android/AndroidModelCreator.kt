package com.chimerapps.discovery.model.connectdialog.android

import com.chimerapps.discovery.device.debugbridge.DebugBridgeBasedDevice
import com.chimerapps.discovery.device.debugbridge.DebugBridgeBootstrap
import com.chimerapps.discovery.model.connectdialog.DeviceModel
import com.chimerapps.discovery.ui.IncludedLibIcons
import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn
import com.icapps.tools.aec.EmulatorFactory
import java.io.File
import javax.swing.Icon

/**
 * @author Nicola Verbeeck
 */
class AndroidModelCreator(
    private val announcementPort: Int,
) {

    private val authToken: String? = try {
        File("${System.getProperty("user.home")}/.emulator_console_auth_token").readText()
    } catch (e: Throwable) {
        null
    }

    fun buildDeviceModel(debugBridgeDevice: DebugBridgeBasedDevice): DeviceModel? {
        @Suppress("LiftReturnOrAssignment")
        try {
            val bootstrap = debugBridgeDevice.bootstrap
            val serial = debugBridgeDevice.serial
            val emulated = bootstrap.executeDebugBridgeCommand("-s", serial, "shell", "getprop", "ro.build.characteristics") == "emulator"
            val name = getCorrectName(bootstrap, serial, emulated)
            val sdkVersion = bootstrap.executeDebugBridgeCommand("-s", serial, "shell", "getprop", "ro.build.version.sdk")
            val version = bootstrap.executeDebugBridgeCommand("-s", serial, "shell", "getprop", "ro.build.version.release")
            val extraInfo = "(Android $version, API $sdkVersion)"

            return DeviceModel(
                name ?: "", extraInfo, getDeviceIcon(emulated),
                serial, debugBridgeDevice, debugBridgeDevice.getSessions(announcementPort)
            )
        } catch (e: Throwable) {
            logger<AndroidModelCreator>().warn("Failed to build device model:", e)
            return null
        }
    }

    private fun getCorrectName(bootstrap: DebugBridgeBootstrap, serial: String, emulated: Boolean): String? {
        return if (emulated) {
            val port = serial.split("-").last().toIntOrNull() ?: return getName(bootstrap, serial)
            if (authToken.isNullOrBlank())
                return getName(bootstrap, serial)

            val emulator = EmulatorFactory.create(port, authToken)
            emulator.connect()
            val output = emulator.avdControl.name()
            emulator.disconnect()
            output.replace("_", " ").trim()
        } else {
            val name = getName(bootstrap, serial)
            val manufacturer = bootstrap.executeDebugBridgeCommand(
                "-s", serial,
                "shell", "getprop", "ro.product.manufacturer"
            )
            "$manufacturer $name"
        }
    }

    private fun getName(adb: DebugBridgeBootstrap, serial: String): String? {
        return adb.executeDebugBridgeCommand("-s", serial, "shell", "getprop", "ro.product.model")
    }

    private fun getDeviceIcon(emulator: Boolean): Icon {
        return if (emulator) IncludedLibIcons.Devices.emulator else IncludedLibIcons.Devices.real
    }

}