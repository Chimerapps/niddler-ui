package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.adb.ADBDevice
import com.icapps.niddler.lib.adb.NiddlerSession

class NiddlerProcessImplementation : NiddlerConnectProcess {
    override fun getProcesses(adbDevice: ADBDevice): List<NiddlerSession> {
        return mutableListOf(NiddlerSession(adbDevice, "be.icapps.project1", 6655, 4564),
                NiddlerSession(adbDevice, "be.icapps.project2", 6656, 4564),
                NiddlerSession(adbDevice, "be.icapps.project3", 6657, 4564))
    }
}