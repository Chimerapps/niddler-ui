package com.chimerapps.niddler.ui.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "NiddlerSettings", storages = [Storage("niddler.xml")])
class NiddlerSettings : PersistentStateComponent<NiddlerSettingsData> {

    companion object {
        val instance: NiddlerSettings
            get() = ApplicationManager.getApplication().getService(NiddlerSettings::class.java)
    }

    private var settings: NiddlerSettingsData = NiddlerSettingsData()

    override fun getState(): NiddlerSettingsData = settings

    override fun loadState(state: NiddlerSettingsData) {
        settings = state
    }

}

data class NiddlerSettingsData(
    var adbPath: String? = null,
    var sdbPath: String? = null,
    var iDeviceBinariesPath: String? = null
)