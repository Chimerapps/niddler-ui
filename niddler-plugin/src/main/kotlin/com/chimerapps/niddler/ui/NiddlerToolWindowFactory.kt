package com.chimerapps.niddler.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/*
 * Entry point of the intellij extension.
 *
 * DumbAware since we don't really care (that much) about indices, especially not when creating the window!
 */
class NiddlerToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentService = ContentFactory.SERVICE.getInstance()

        val window = NiddlerToolWindow()

        val content = contentService.createContent(window, "", true)
        toolWindow.contentManager.addContent(content)
    }

}