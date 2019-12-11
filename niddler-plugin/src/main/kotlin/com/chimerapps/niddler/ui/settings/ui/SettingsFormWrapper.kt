package com.chimerapps.niddler.ui.settings.ui

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.niddler.ui.settings.NiddlerSettings
import com.chimerapps.niddler.ui.util.adb.ADBUtils
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JTextPane
import javax.swing.SwingWorker

class SettingsFormWrapper(private val niddlerSettings: NiddlerSettings) {

    private val settingsForm = NiddlerSettingsForm()

    val component: JComponent
        get() = settingsForm.`$$$getRootComponent$$$`()

    val isModified: Boolean
        get() = ((settingsForm.iDeviceField.textOrNull != (niddlerSettings.iDeviceBinariesPath))
                || (settingsForm.adbField.textOrNull != (niddlerSettings.adbPath)))

    private var worker: VerifierWorker? = null

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
        worker?.cancel(true)
        settingsForm.resultsPane.text = ""
        settingsForm.testConfigurationButton.isEnabled = false

        worker = VerifierWorker(settingsForm.adbField.textOrNull ?: ADBBootstrap(ADBUtils.guessPaths(project)).pathToAdb,
                settingsForm.iDeviceField.textOrNull ?: "/usr/local/bin",
                settingsForm.resultsPane, settingsForm.testConfigurationButton).also {
            it.execute()
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

private class VerifierWorker(private val adbPath: String?,
                             private val iDevicePath: String,
                             private val textField: JTextPane,
                             private val button: JButton) : SwingWorker<Boolean, String>() {

    private val builder = StringBuilder()

    override fun doInBackground(): Boolean {
        val adbResult = testADB()
        val iDeviceResult = testIDevice()
        return adbResult && iDeviceResult
    }

    override fun process(chunks: List<String>) {
        chunks.forEach { builder.append(it).append('\n') }

        textField.text = builder.toString()
        textField.invalidate()
    }

    override fun done() {
        super.done()
        button.isEnabled = true
    }

    private fun testADB(): Boolean {
        publish("Testing ADB\n=======================================")
        var ok = false
        if (adbPath == null) {
            publish("Path to ADB not found")
        } else {
            publish("ADB defined at path: $adbPath")
            val file = File(adbPath)
            if (file.isDirectory) {
                publish("ERROR - Specified path is a directory")
            } else if (!file.exists()) {
                publish("ERROR - ADB file not found")
            } else if (!file.canExecute()) {
                publish("ERROR - ADB file not executable")
            } else {
                publish("ADB path seems ok")
                ok = true
            }
        }
        return ok && checkADBExecutable()
    }

    private fun testIDevice(): Boolean {
        publish("\nTesting iDevice\n=======================================")

        publish("iMobileDevice folder defined at path: $iDevicePath")
        val file = File(iDevicePath)
        if (!file.exists()) {
            publish("ERROR - iMobileDevice folder not found")
        } else if (!file.isDirectory) {
            publish("ERROR - iMobileDevice path is not a directory")
        } else {
            checkFile(File(file, "ideviceinfo"))
            checkFile(File(file, "iproxy"))
            checkFile(File(file, "idevice_id"))
        }

        return true //TODO
    }

    private fun checkFile(file: File) {
        if (!file.exists()) {
            publish("ERROR - ${file.name} file not found")
        } else if (!file.canExecute()) {
            publish("ERROR - ${file.name} file not executable")
        } else {
            publish("${file.name} seems ok")
        }
    }

    private fun checkADBExecutable(): Boolean {
        publish("Checking adb command")
        val bootstrap = ADBBootstrap(emptyList()) { adbPath!! }
        publish("Starting adb server")
        try {
            val adbInterface = bootstrap.bootStrap()
            publish("Listing devices")
            val devices = adbInterface.devices
            publish("ADB devices returns: ${devices.size} devices")
            return true
        } catch (e: Exception) {
            publish("ERROR - Failed to communicate with adb")
            val writer = StringWriter()
            val printer = PrintWriter(writer)
            e.printStackTrace(printer)
            printer.flush()
            publish(writer.toString())
            return false
        }
    }

}