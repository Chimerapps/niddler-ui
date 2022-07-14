package com.chimerapps.discovery.device.adb

import com.chimerapps.discovery.device.debugbridge.DebugBridgeBasedDevice
import com.chimerapps.discovery.device.debugbridge.DebugBridgeBootstrap
import se.vidstige.jadb.JadbDevice

class ADBDevice(
    device: JadbDevice,
    bootstrap: DebugBridgeBootstrap,
) : DebugBridgeBasedDevice(device, bootstrap)
