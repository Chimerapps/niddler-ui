package com.chimerapps.discovery.ui

interface ConnectDialogLocalization {

    val noConnectedDevices: String get() = "No connected devices"
    val port: String get() = "Port"
    val dialogTitle: String get() = "Select a device to connect to"
    val adbPathNotFound: String get() = "ADB path not found"
    val iDevicePathNotFound: String get() = "iDevice path not found"
    val invalidPort: String get() = "Invalid port"
    val invalidPortTitle: String get() = "Could not connect"
    val cancel: String get() = "Cancel"
    val connect: String get() = "Connect"
    val deviceIp: String get() = "Device IP:"
    val portLabel: String get() = "Port:"
}
