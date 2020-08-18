package com.chimerapps.niddler.ui.settings.ui

import com.chimerapps.niddler.ui.settings.NiddlerProjectSettings
import com.chimerapps.niddler.ui.util.localization.Tr
import javax.swing.JComponent

class ProjectSettingsFormWrapper(private val niddlerSettings: NiddlerProjectSettings) {

    private val settingsForm = NiddlerProjectSettingsForm()

    val component: JComponent
        get() = settingsForm.root

    val isModified: Boolean
        get() = ((settingsForm.reconnectCheckbox.isSelected != (niddlerSettings.automaticallyReconnect))
                || (settingsForm.reuseCheckbox.isSelected != (niddlerSettings.reuseSession))
                || (settingsForm.connectUsingDebuggerCheckbox.isSelected != (niddlerSettings.connectUsingDebugger)))

    init{
        settingsForm.reconnectCheckbox.text = Tr.PreferencesOptionReconnect.tr()
        settingsForm.reuseCheckbox.text = Tr.PreferencesOptionReuseConnection.tr()
        settingsForm.connectUsingDebuggerCheckbox.text = Tr.PreferencesOptionConnectDebugger.tr()
    }

    fun save() {
        niddlerSettings.reuseSession = settingsForm.reuseCheckbox.isSelected
        niddlerSettings.automaticallyReconnect = settingsForm.reconnectCheckbox.isSelected
        niddlerSettings.connectUsingDebugger = settingsForm.connectUsingDebuggerCheckbox.isSelected
    }

    fun reset() {
        settingsForm.reconnectCheckbox.isSelected = niddlerSettings.automaticallyReconnect ?: false
        settingsForm.reuseCheckbox.isSelected = niddlerSettings.reuseSession ?: false
        settingsForm.connectUsingDebuggerCheckbox.isSelected = niddlerSettings.connectUsingDebugger ?: false
    }
    
}
