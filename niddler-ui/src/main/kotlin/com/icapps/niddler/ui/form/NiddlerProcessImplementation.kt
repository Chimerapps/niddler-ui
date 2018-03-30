package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.adb.ADBDevice
import com.icapps.niddler.lib.adb.NiddlerSession

class NiddlerProcessImplementation : NiddlerConnectProcess {
    override fun getProcesses(adbDevice: ADBDevice): List<NiddlerSession> {
        return adbDevice.getNiddlerSessions() ?: emptyList()
    }
}