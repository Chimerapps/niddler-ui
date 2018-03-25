package com.icapps.niddler.ui.model

import com.icapps.niddler.lib.adb.ADBDevice

data class AdbDeviceModel(val name: String,
                          val extraInfo: String,
                          val emulator: Boolean,
                          val serialNr: String,
                          val device: ADBDevice)