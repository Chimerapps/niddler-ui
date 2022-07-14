package com.chimerapps.discovery.device.sdb

import com.chimerapps.discovery.device.debugbridge.DebugBridgeBasedDevice
import com.chimerapps.discovery.device.debugbridge.DebugBridgeBootstrap
import se.vidstige.jadb.JadbDevice

class SDBDevice(
    device: JadbDevice,
    bootstrap: DebugBridgeBootstrap,
) : DebugBridgeBasedDevice(device, bootstrap)
