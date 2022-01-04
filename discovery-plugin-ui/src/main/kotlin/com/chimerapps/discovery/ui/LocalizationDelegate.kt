package com.chimerapps.discovery.ui

/**
 * @author Nicola Verbeeck
 */
open class LocalizationDelegate {
    open val connectDialogTitle = "Select a device to connect to"
    open val statusADBPathNotFound = "ADB path not found"
    open val statusIDevicePathNotFound = "iDevice path not found"
    open val errorMessageInvalidPort = "Invalid port"
    open val errorTitleInvalidPort = "Could not connect"

    open val buttonCancel = "Cancel"
    open val buttonConnect = "Connect"
    open val deviceIp = "Device ip:"
    open val processPort = "Port:"

    open val noDeviceFound = "No connected devices"
    open fun processPort(port: Int) = " (port: $port)"
}