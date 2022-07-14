package com.chimerapps.discovery.device.adb

import com.chimerapps.discovery.device.debugbridge.DebugBridgeBootstrap
import com.chimerapps.discovery.device.sdb.SDBDevice

/**
 * @author Nicola Verbeeck
 */
class ADBBootstrap(
    sdkPathGuesses: Collection<String>,
    adbPathProvider: (() -> String?)? = null,
) : DebugBridgeBootstrap(
    toolExecutableName = "adb",
    toolName = "android",
    sdkPathEnvironmentVariable = "ANDROID_HOME",
    sdkPathGuesses = sdkPathGuesses,
    debugBridgePathProvider = adbPathProvider,
    deviceBridgeServerPort = 5037,
    deviceCreator = { device, bootstrap -> SDBDevice(device, bootstrap) },
    sdkToolSubDirectory = "platform-tools",
)