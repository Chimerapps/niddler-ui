package com.chimerapps.niddler.ui.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean

@State(name = "NiddlerSettings")
class NiddlerProjectSettings : PersistentStateComponent<NiddlerProjectSettings> {

    companion object {
        fun instance(project: Project): NiddlerProjectSettings {
            return ServiceManager.getService(project, NiddlerProjectSettings::class.java)
        }
    }

    var automaticallyReconnect: Boolean? = null
    var reuseSession: Boolean? = null

    override fun getState(): NiddlerProjectSettings? = this

    override fun loadState(state: NiddlerProjectSettings) {
        copyBean(state, this)
    }

}