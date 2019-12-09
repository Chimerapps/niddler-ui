package com.chimerapps.niddler.ui.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean

@State(name = "NiddlerSettings", storages = [Storage("niddler.xml")])
class NiddlerSettings : PersistentStateComponent<NiddlerSettings> {

    companion object {
        val instance: NiddlerSettings
            get() = getService(NiddlerSettings::class.java)
    }

    var adbPath: String? = null
    var iDeviceBinariesPath: String? = null

    override fun getState(): NiddlerSettings? = this

    override fun loadState(state: NiddlerSettings) {
        copyBean(state, this)
    }

}