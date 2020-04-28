package com.chimerapps.niddler.ui.settings

import com.chimerapps.niddler.ui.settings.ui.ProjectSettingsFormWrapper
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class ProjectConfigurationProvider(private val project: Project) : Configurable {

    private var settingsForm: ProjectSettingsFormWrapper? = null

    override fun isModified(): Boolean = settingsForm?.isModified ?: false

    override fun getDisplayName(): String = "Per project"

    override fun apply() {
        settingsForm?.save()
    }

    override fun reset() {
        settingsForm?.reset()
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
        settingsForm = null
    }

    override fun createComponent(): JComponent? {
        val form = settingsForm ?: ProjectSettingsFormWrapper(NiddlerProjectSettings.instance(project))
        settingsForm = form
        return form.component
    }

}