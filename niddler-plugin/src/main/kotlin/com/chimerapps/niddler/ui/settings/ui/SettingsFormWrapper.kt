package com.chimerapps.niddler.ui.settings.ui

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.niddler.ui.settings.NiddlerSettings
import com.chimerapps.niddler.ui.util.adb.ADBUtils
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent

class SettingsFormWrapper(private val niddlerSettings: NiddlerSettings) {

    private val settingsForm = NiddlerSettingsForm()

    val component: JComponent
        get() = settingsForm.`$$$getRootComponent$$$`()

    val isModified: Boolean
        get() = ((settingsForm.iDeviceField.text != (niddlerSettings.iDeviceBinariesPath ?: ""))
                || (settingsForm.adbField.text != (niddlerSettings.adbPath ?: "")))

    fun save() {
        settingsForm.iDeviceField.text.trim().let {
            if (it.isEmpty())
                niddlerSettings.iDeviceBinariesPath = null
            else
                niddlerSettings.iDeviceBinariesPath = it
        }
        settingsForm.adbField.text.trim().let {
            if (it.isEmpty())
                niddlerSettings.adbPath = null
            else
                niddlerSettings.adbPath = it
        }
    }

    fun initUI(project: Project? = null) {
        settingsForm.adbField.addBrowseFolderListener("Niddler - adb", "Path to adb", project, FileChooserDescriptor(true, false, false, false, false, false))
        settingsForm.iDeviceField.addBrowseFolderListener("Niddler - imobiledevice", "Path to imobiledevice folders", project, FileChooserDescriptor(false, true, false, false, false, false))

        (settingsForm.iDeviceField.textField as? JBTextField)?.emptyText?.text = "/usr/local/bin"

        (settingsForm.adbField.textField as? JBTextField)?.emptyText?.text = ADBBootstrap(ADBUtils.guessPaths(project)).pathToAdb ?: ""

        reset()
    }

    fun reset() {
        settingsForm.adbField.text = niddlerSettings.adbPath ?: ""
        settingsForm.iDeviceField.text = niddlerSettings.iDeviceBinariesPath ?: ""
    }

}