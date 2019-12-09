package com.chimerapps.niddler.ui.settings.ui

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.niddler.ui.settings.NiddlerSettings
import com.chimerapps.niddler.ui.util.adb.ADBUtils
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import java.io.File
import javax.swing.JComponent

class SettingsFormWrapper(private val niddlerSettings: NiddlerSettings) {

    private val settingsForm = NiddlerSettingsForm()

    val component: JComponent
        get() = settingsForm.`$$$getRootComponent$$$`()

    val isModified: Boolean
        get() = ((settingsForm.iDeviceField.textOrNull != (niddlerSettings.iDeviceBinariesPath))
                || (settingsForm.adbField.textOrNull != (niddlerSettings.adbPath)))

    fun save() {
        settingsForm.iDeviceField.textOrNull?.let {
            niddlerSettings.iDeviceBinariesPath = it
        }
        settingsForm.adbField.textOrNull?.let {
            niddlerSettings.adbPath = it
        }
    }

    fun initUI(project: Project? = null) {
        settingsForm.adbField.addBrowseFolderListener("Niddler - adb", "Path to adb", project, FileChooserDescriptor(true, false, false, false, false, false))
        settingsForm.iDeviceField.addBrowseFolderListener("Niddler - imobiledevice", "Path to imobiledevice folders", project, FileChooserDescriptor(false, true, false, false, false, false))

        (settingsForm.iDeviceField.textField as? JBTextField)?.emptyText?.text = "/usr/local/bin"

        (settingsForm.adbField.textField as? JBTextField)?.emptyText?.text = ADBBootstrap(ADBUtils.guessPaths(project)).pathToAdb ?: ""

        settingsForm.testConfigurationButton.addActionListener {
            runTest(project)
        }

        reset()
    }

    fun reset() {
        settingsForm.adbField.text = niddlerSettings.adbPath ?: ""
        settingsForm.iDeviceField.text = niddlerSettings.iDeviceBinariesPath ?: ""
    }

    private fun runTest(project: Project?) {
        settingsForm.resultsPane.text = ""

        val stringBuilder = StringBuilder()

        stringBuilder.append("Testing ADB\n=======================================\n")

        val adb = settingsForm.adbField.textOrNull ?: ADBBootstrap(ADBUtils.guessPaths(project)).pathToAdb
        if (adb == null) {
            stringBuilder.append("Path to ADB not found\n")
        } else {
            stringBuilder.append("ADB defined at path: $adb\n")
            val file = File(adb)
            if (!file.exists()) {
                stringBuilder.append("ERROR - ADB file not found\n")
            } else if (!file.canExecute()) {
                stringBuilder.append("ERROR - ADB file not executable\n")
            } else {
                stringBuilder.append("ADB seems ok\n")
            }
        }

        stringBuilder.append("\nTesting iDevice\n=======================================\n")

        val iDevice = settingsForm.iDeviceField.textOrNull ?: "/usr/local/bin/"
        stringBuilder.append("iMobileDevice folder defined at path: $iDevice\n")
        val file = File(iDevice)
        if (!file.exists()) {
            stringBuilder.append("ERROR - iMobileDevice folder not found\n")
        } else if (!file.isDirectory) {
            stringBuilder.append("ERROR - iMobileDevice path is not a directory\n")
        } else {
            checkFile(File(file, "ideviceinfo"), stringBuilder)
            checkFile(File(file, "iproxy"), stringBuilder)
            checkFile(File(file, "idevice_id"), stringBuilder)
        }

        settingsForm.resultsPane.text = stringBuilder.toString()
        settingsForm.resultsPane.invalidate()
    }

    private fun checkFile(file: File, stringBuilder: StringBuilder) {
        if (!file.exists()) {
            stringBuilder.append("ERROR - ${file.name} file not found\n")
        } else if (!file.canExecute()) {
            stringBuilder.append("ERROR - ${file.name} file not executable\n")
        } else {
            stringBuilder.append("${file.name} seems ok\n")
        }
    }

}

private val TextFieldWithBrowseButton.textOrNull: String?
    get() {
        val textValue = text.trim()
        if (textValue.isEmpty())
            return null
        return textValue
    }
