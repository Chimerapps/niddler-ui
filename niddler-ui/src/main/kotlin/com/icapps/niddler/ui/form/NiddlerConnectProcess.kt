package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.adb.ADBDevice
import com.icapps.niddler.lib.adb.NiddlerSession

/**
 * @author Koen Van Looveren
 */
interface NiddlerConnectProcess {

    fun getProcesses(adbDevice: ADBDevice): List<NiddlerSession>
}