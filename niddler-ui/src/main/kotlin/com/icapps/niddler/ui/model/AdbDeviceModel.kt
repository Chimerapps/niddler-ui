package com.icapps.niddler.ui.model

import com.icapps.niddler.lib.device.Device
import com.icapps.niddler.lib.device.NiddlerSession
import javax.swing.Icon

class DeviceModel(val name: String?,
                  val extraInfo: String,
                  val icon: Icon,
                  val serialNr: String,
                  val sessions: List<NiddlerSession>,
                  val device: Device) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceModel

        if (serialNr != other.serialNr) return false

        return true
    }

    override fun hashCode(): Int {
        return serialNr.hashCode()
    }
}