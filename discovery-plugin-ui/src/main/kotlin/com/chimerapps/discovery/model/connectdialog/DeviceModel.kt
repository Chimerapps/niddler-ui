package com.chimerapps.discovery.model.connectdialog

import com.chimerapps.discovery.device.Device
import com.chimerapps.discovery.device.DiscoveredSession
import javax.swing.Icon

class DeviceModel(val name: String?,
                  val extraInfo: String,
                  val icon: Icon,
                  val serialNr: String,
                  val device: Device,
                  val sessions: List<DiscoveredSession>) {

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