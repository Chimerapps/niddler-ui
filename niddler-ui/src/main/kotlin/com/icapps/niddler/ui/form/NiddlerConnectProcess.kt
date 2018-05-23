package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.device.adb.ADBDevice
import com.icapps.niddler.lib.device.NiddlerSession

/**
 * @author Koen Van Looveren
 */
interface NiddlerConnectProcess {

    fun getProcesses(adbDevice: ADBDevice): List<NiddlerSession>
}