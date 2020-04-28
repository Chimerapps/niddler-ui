package com.chimerapps.niddler.ui.settings.ui

import com.chimerapps.niddler.ui.settings.NiddlerProjectSettings
import javax.swing.JComponent

class ProjectSettingsFormWrapper(private val niddlerSettings: NiddlerProjectSettings) {

    private val settingsForm = NiddlerProjectSettingsForm()

    val component: JComponent
        get() = settingsForm.root

    val isModified: Boolean
        get() = ((settingsForm.reconnectCheckbox.isSelected != (niddlerSettings.automaticallyReconnect))
                || (settingsForm.reuseCheckbox.isSelected != (niddlerSettings.reuseSession)))

    fun save() {
        niddlerSettings.reuseSession = settingsForm.reuseCheckbox.isSelected
        niddlerSettings.automaticallyReconnect = settingsForm.reconnectCheckbox.isSelected
    }

    fun reset() {
        settingsForm.reconnectCheckbox.isSelected = niddlerSettings.automaticallyReconnect ?: false
        settingsForm.reuseCheckbox.isSelected = niddlerSettings.reuseSession ?: false
    }
    
}
