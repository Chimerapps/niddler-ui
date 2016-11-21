package com.icapps.niddler.ui

import com.icapps.niddler.ui.component.IntelliJInterfaceFactory
import com.icapps.niddler.ui.form.NiddlerWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class NiddlerToolWindow : ToolWindowFactory {

    private lateinit var niddlerWindow: NiddlerWindow

    override fun createToolWindowContent(p0: Project, window: ToolWindow) {
        niddlerWindow = NiddlerWindow(IntelliJInterfaceFactory(p0, window.contentManager))

        val contentService = ContentFactory.SERVICE.getInstance()
        val content = contentService.createContent(niddlerWindow, "Niddler", true)
        niddlerWindow.init()
        window.contentManager.addContent(content)
    }

}