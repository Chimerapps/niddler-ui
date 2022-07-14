package com.chimerapps.niddler.ui.settings.ui

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.discovery.device.idevice.IDeviceBootstrap
import com.chimerapps.discovery.device.sdb.SDBBootstrap
import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.settings.NiddlerSettings
import com.chimerapps.niddler.ui.util.adb.ADBUtils
import com.chimerapps.niddler.ui.util.sdb.SDBUtils
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.wm.WindowManager
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
        get() = ((settingsForm.iDeviceField.textOrNull != (niddlerSettings.state.iDeviceBinariesPath))
                || (settingsForm.adbField.textOrNull != (niddlerSettings.state.adbPath)))

    private var worker: VerifierWorker? = null

    fun save() {
        niddlerSettings.state.iDeviceBinariesPath = settingsForm.iDeviceField.textOrNull
        niddlerSettings.state.adbPath = settingsForm.adbField.textOrNull
        niddlerSettings.state.sdbPath = settingsForm.sdbField.textOrNull

        val project = ProjectManager.getInstance().openProjects.firstOrNull { project ->
            val window = WindowManager.getInstance().suggestParentWindow(project)
            window != null && window.isActive
        } ?: return
        NiddlerToolWindow.get(project)?.first?.redoADBBootstrapBlocking()
    }

    fun initUI(project: Project? = null) {
        settingsForm.adbField.addBrowseFolderListener(
            "Niddler - adb", "Path to adb",
            project,
            FileChooserDescriptor(true, false, false, false, false, false)
        )
        settingsForm.iDeviceField.addBrowseFolderListener(
            "Niddler - imobiledevice", "Path to imobiledevice folders",
            project,
            FileChooserDescriptor(false, true, false, false, false, false)
        )
        settingsForm.sdbField.addBrowseFolderListener(
            "Niddler - sdb", "Path to sdb",
            project,
            FileChooserDescriptor(true, false, false, false, false, false)
        )

        (settingsForm.iDeviceField.textField as? JBTextField)?.emptyText?.text = "/usr/local/bin"

        (settingsForm.adbField.textField as? JBTextField)?.emptyText?.text = ADBBootstrap(ADBUtils.guessPaths(project)).pathToDebugBridge ?: ""
        (settingsForm.sdbField.textField as? JBTextField)?.emptyText?.text = ADBBootstrap(SDBUtils.guessPaths(project)).pathToDebugBridge ?: ""

        settingsForm.testConfigurationButton.addActionListener {
            runTest(project)
        }

        reset()
    }

    fun reset() {
        settingsForm.adbField.text = niddlerSettings.state.adbPath ?: ""
        settingsForm.sdbField.text = niddlerSettings.state.sdbPath ?: ""
        settingsForm.iDeviceField.text = niddlerSettings.state.iDeviceBinariesPath ?: ""
    }

    private fun runTest(project: Project?) {
        worker?.cancel(true)
        settingsForm.resultsPane.text = ""
        settingsForm.testConfigurationButton.isEnabled = false

        worker = VerifierWorker(
            settingsForm.adbField.textOrNull ?: ADBBootstrap(ADBUtils.guessPaths(project)).pathToDebugBridge,
            settingsForm.sdbField.textOrNull ?: SDBBootstrap(SDBUtils.guessPaths(project)).pathToDebugBridge,
            settingsForm.iDeviceField.textOrNull ?: "/usr/local/bin",
            settingsForm.resultsPane, settingsForm.testConfigurationButton
        ).also {
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

private class VerifierWorker(
    private val adbPath: String?,
    private val sdbPath: String?,
    private val iDevicePath: String,
    private val textField: JTextPane,
    private val button: JButton
) : SwingWorker<Boolean, String>() {

    private val builder = StringBuilder()

    override fun doInBackground(): Boolean {
        val adbResult = testADB()
        val sdbResult = testSDB()
        val iDeviceResult = testIDevice()
        return adbResult && iDeviceResult && sdbResult
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

    private fun testSDB(): Boolean {
        publish("\nTesting SDB\n=======================================")
        var ok = false
        if (sdbPath == null) {
            publish("Path to SDB not found")
        } else {
            publish("SDB defined at path: $sdbPath")
            val file = File(sdbPath)
            if (file.isDirectory) {
                publish("ERROR - Specified path is a directory")
            } else if (!file.exists()) {
                publish("ERROR - SDB file not found")
            } else if (!file.canExecute()) {
                publish("ERROR - SDB file not executable")
            } else {
                publish("SDB path seems ok")
                ok = true
            }
        }
        return ok && checkSDBExecutable()
    }

    private fun testIDevice(): Boolean {
        publish("\nTesting iDevice\n=======================================")

        publish("iMobileDevice folder defined at path: $iDevicePath")
        val file = File(iDevicePath)
        var ok = true
        if (!file.exists()) {
            publish("ERROR - iMobileDevice folder not found")
            ok = false
        } else if (!file.isDirectory) {
            publish("ERROR - iMobileDevice path is not a directory")
            ok = false
        } else {
            ok = ok && checkFile(File(file, "ideviceinfo"))
            ok = ok && checkFile(File(file, "iproxy"))
            ok = ok && checkFile(File(file, "idevice_id"))
        }

        if (ok) {
            try {
                publish("Listing devices")
                val devices = IDeviceBootstrap(file).devices
                publish("iDevice `idevice_id -l` returns: ${devices.size} devices")
            } catch (e: Throwable) {
                publish("ERROR - Failed to communicate with idevice_id")
                val writer = StringWriter()
                val printer = PrintWriter(writer)
                e.printStackTrace(printer)
                printer.flush()
                publish(writer.toString())
                ok = false
            }
        }

        return ok
    }

    private fun checkFile(file: File): Boolean {
        return if (!file.exists()) {
            publish("ERROR - ${file.name} file not found")
            false
        } else if (!file.canExecute()) {
            publish("ERROR - ${file.name} file not executable")
            false
        } else {
            publish("${file.name} seems ok")
            true
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
        } catch (e: Throwable) {
            publish("ERROR - Failed to communicate with adb")
            val writer = StringWriter()
            val printer = PrintWriter(writer)
            e.printStackTrace(printer)
            printer.flush()
            publish(writer.toString())
            return false
        }
    }

    private fun checkSDBExecutable(): Boolean {
        publish("Checking sdb command")
        val bootstrap = SDBBootstrap(emptyList()) { sdbPath!! }
        publish("Starting sdb server")
        return try {
            val sdbInterface = bootstrap.bootStrap()
            publish("Listing devices")
            val devices = sdbInterface.devices
            publish("SDB devices returns: ${devices.size} devices")
            true
        } catch (e: Throwable) {
            publish("ERROR - Failed to communicate with sdb")
            val writer = StringWriter()
            val printer = PrintWriter(writer)
            e.printStackTrace(printer)
            printer.flush()
            publish(writer.toString())
            false
        }
    }

}