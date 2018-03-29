package com.icapps.niddler.ui.model

import com.icapps.niddler.lib.adb.ADBDevice

class AdbDeviceModel(val name: String,
                     val extraInfo: String,
                     val emulator: Boolean,
                     val serialNr: String,
                     val processes: List<String>,
                     val device: ADBDevice) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdbDeviceModel

        if (serialNr != other.serialNr) return false

        return true
    }

    override fun hashCode(): Int {
        return serialNr.hashCode()
    }
}