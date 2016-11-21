package com.icapps.niddler.ui

import com.icapps.niddler.ui.form.NiddlerWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

/**
 * @author Nicola Verbeeck
 *
 * @date 21/11/16.
 */
class NiddlerToolWindow : ToolWindowFactory {

    private val niddlerWindow = NiddlerWindow()

    override fun createToolWindowContent(p0: Project, window: ToolWindow) {
        val contentService = ContentFactory.SERVICE.getInstance()
        val content = contentService.createContent(niddlerWindow, "Niddler", true)

        niddlerWindow.init()
        niddlerWindow.onWindowVisible()

        window.contentManager.addContent(content)
        window.component.addAncestorListener(object: AncestorListener{
            override fun ancestorAdded(event: AncestorEvent?) {
                niddlerWindow.onWindowVisible()
            }

            override fun ancestorMoved(event: AncestorEvent?) {
                // Do nothing
            }

            override fun ancestorRemoved(event: AncestorEvent?) {
                niddlerWindow.onWindowInvisible()
            }
        })
    }

}