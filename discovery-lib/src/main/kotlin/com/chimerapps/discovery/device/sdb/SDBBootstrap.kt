package com.chimerapps.discovery.device.sdb

import com.chimerapps.discovery.device.debugbridge.DebugBridgeBootstrap

class SDBBootstrap(
    sdkPathGuesses: Collection<String>,
    sdbPathProvider: (() -> String?)? = null,
) : DebugBridgeBootstrap(
    toolExecutableName = "sdb",
    toolName = "tizen",
    sdkPathEnvironmentVariable = "TIZEN_SDK",
    sdkPathGuesses = sdkPathGuesses,
    debugBridgePathProvider = sdbPathProvider,
    deviceBridgeServerPort = 26099,
    deviceCreator = { device, bootstrap -> SDBDevice(device, bootstrap) },
    sdkToolSubDirectory = "tools"
)