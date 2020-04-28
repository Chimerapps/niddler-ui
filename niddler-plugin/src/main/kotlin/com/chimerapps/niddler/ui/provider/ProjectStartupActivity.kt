package com.chimerapps.niddler.ui.provider

import com.chimerapps.niddler.ui.util.execution.ProcessExecutionListener
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class ProjectStartupActivity : StartupActivity, DumbAware {

    override fun runActivity(p0: Project) {
        ProcessExecutionListener(p0)
    }

}