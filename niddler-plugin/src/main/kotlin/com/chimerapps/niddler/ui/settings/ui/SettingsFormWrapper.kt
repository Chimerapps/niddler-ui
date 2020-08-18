package com.chimerapps.niddler.ui.settings.ui

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.settings.NiddlerSettings
import com.chimerapps.niddler.ui.util.adb.ADBUtils
import com.chimerapps.niddler.ui.util.localization.Tr
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

    init {
        settingsForm.pathToAdbLabel.text = Tr.PreferencesOptionPathToAdb.tr()
        settingsForm.pathToiDeviceLabel.text = Tr.PreferencesOptionPathToIdevice.tr()
        settingsForm.testConfigurationButton.text = Tr.PreferencesButtonTestConfiguration.tr()
    }

    fun save() {
        settingsForm.iDeviceField.textOrNull?.let {
            niddlerSettings.iDeviceBinariesPath = it
        }
        settingsForm.adbField.textOrNull?.let {
            niddlerSettings.adbPath = it
        }
    }

    fun initUI(project: Project? = null) {
        settingsForm.adbField.addBrowseFolderListener(Tr.PreferencesBrowseAdbTitle.tr(), Tr.PreferencesBrowseAdbDescription.tr(), project, FileChooserDescriptor(true, false, false, false, false, false))
        settingsForm.iDeviceField.addBrowseFolderListener(Tr.PreferencesBrowseIdeviceTitle.tr(), Tr.PreferencesBrowseIdeviceDescription.tr(), project, FileChooserDescriptor(false, true, false, false, false, false))

        (settingsForm.iDeviceField.textField as? JBTextField)?.emptyText?.text = NiddlerSessionWindow.DEFAULT_IDEVICE_PATH

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
                settingsForm.iDeviceField.textOrNull ?: NiddlerSessionWindow.DEFAULT_IDEVICE_PATH,
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
        publish(Tr.PreferencesTestMessageTestingAdbTitle.tr())
        var ok = false
        if (adbPath == null) {
            publish(Tr.PreferencesTestMessageAdbNotFound.tr())
        } else {
            publish(Tr.PreferencesTestMessageAdbFoundAt.tr(adbPath))
            val file = File(adbPath)
            if (file.isDirectory) {
                publish(Tr.PreferencesTestMessageErrorPathIsDir.tr())
            } else if (!file.exists()) {
                publish(Tr.PreferencesTestMessageAdbNotFound.tr())
            } else if (!file.canExecute()) {
                publish(Tr.PreferencesTestMessageErrorAdbNotExecutable.tr())
            } else {
                publish(Tr.PreferencesTestMessageAdbOk.tr())
                ok = true
            }
        }
        return ok && checkADBExecutable()
    }

    private fun testIDevice(): Boolean {
        publish(Tr.PreferencesTestMessageTestingIdeviceTitle.tr())

        publish(Tr.PreferencesTestMessageIdevicePath.tr(iDevicePath))
        val file = File(iDevicePath)
        if (!file.exists()) {
            publish(Tr.PreferencesTestMessageErrorIdeviceNotFound.tr())
        } else if (!file.isDirectory) {
            publish(Tr.PreferencesTestMessageErrorIdeviceNotDirectory.tr())
        } else {
            checkFile(File(file, "ideviceinfo"))
            checkFile(File(file, "iproxy"))
            checkFile(File(file, "idevice_id"))
        }

        return true //TODO
    }

    private fun checkFile(file: File) {
        if (!file.exists()) {
            publish(Tr.PreferencesTestMessageErrorFileNotFound.tr(file.name))
        } else if (!file.canExecute()) {
            publish(Tr.PreferencesTestMessageErrorFileNotExecutable.tr(file.name))
        } else {
            publish(Tr.PreferencesTestMessageFileOk.tr(file.name))
        }
    }

    private fun checkADBExecutable(): Boolean {
        publish(Tr.PreferencesTestMessageCheckingAdb.tr())
        val bootstrap = ADBBootstrap(emptyList()) { adbPath!! }
        publish(Tr.PreferencesTestMessageStartingAdb.tr())
        try {
            val adbInterface = bootstrap.bootStrap()
            publish(Tr.PreferencesTestMessageListingAdbDevices.tr())
            val devices = adbInterface.devices
            publish(Tr.PreferencesTestMessageFoundDevicesCount.tr(devices.size))
            return true
        } catch (e: Exception) {
            publish(Tr.PreferencesTestMessageErrorCommunicationFailed.tr())
            val writer = StringWriter()
            val printer = PrintWriter(writer)
            e.printStackTrace(printer)
            printer.flush()
            publish(writer.toString())
            return false
        }
    }

}