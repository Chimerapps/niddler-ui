package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.device.adb.ADBDevice
import com.icapps.niddler.lib.device.NiddlerSession

class NiddlerProcessImplementation : NiddlerConnectProcess {
    override fun getProcesses(adbDevice: ADBDevice): List<NiddlerSession> {
        return adbDevice.getNiddlerSessions()
    }
}