package com.icapps.niddler.ui

import com.icapps.niddler.lib.utils.Logger
import com.icapps.niddler.lib.utils.LoggerFactory
import com.icapps.niddler.lib.utils.info
import com.icapps.niddler.lib.utils.logger
import com.icapps.niddler.ui.component.IntelliJComponentsFactory
import com.icapps.niddler.ui.form.MainThreadDispatcher
import com.icapps.niddler.ui.form.NiddlerWindow
import com.icapps.niddler.ui.impl.IntelliJNiddlerUserInterface
import com.icapps.niddler.ui.util.ImageHelper
import com.icapps.niddler.ui.util.iconLoader
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.util.HashSet
import javax.swing.Icon
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

/**
 * @author Nicola Verbeeck
 *
 * @date 21/11/16.
 */
class NiddlerToolWindow : ToolWindowFactory, DumbAware {

    companion object {
        init {
            initLogger()
        }

        private val log = logger<NiddlerToolWindow>()
    }

    private lateinit var niddlerWindow: NiddlerWindow

    override fun createToolWindowContent(p0: Project, window: ToolWindow) {
        MainThreadDispatcher.instance = IntelliJMaiThreadDispatcher()
        iconLoader = IntellijIconLoader()

        val ui = IntelliJNiddlerUserInterface(p0, IntelliJComponentsFactory(p0, window.contentManager))
        niddlerWindow = NiddlerWindow(ui, guessPaths(p0))

        val contentService = ContentFactory.SERVICE.getInstance()
        val content = contentService.createContent(ui.asComponent, "Inspect network traffic", true)

        niddlerWindow.init()
        niddlerWindow.onWindowVisible()

        window.contentManager.addContent(content)
        window.component.addAncestorListener(object : AncestorListener {
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

    private fun guessPaths(project: Project): Collection<String> {
        val paths = HashSet<String>()
        val pathProperty = System.getProperty("android.sdk.path")
        if (pathProperty != null) {
            log.info("Got android sdk path from property: $pathProperty")
            paths += pathProperty
        }
        val fromProperties = PropertiesComponent.getInstance(project).getValue("android.sdk.path")
        if (fromProperties != null) {
            log.info("Got android sdk path from project properties: $fromProperties")
            paths += fromProperties
        }

        return paths
    }

}

private class IntellijIconLoader : ImageHelper {

    override fun loadImage(clazz: Class<*>, path: String): Icon {
        return IconLoader.getIcon(path, clazz)
    }

}

private fun initLogger() {
    LoggerFactory.instance = IdeaLoggerFactory()
}

private class IdeaLoggerFactory : LoggerFactory {

    override fun getInstanceForClass(clazz: Class<*>): Logger {
        return LoggerWrapper(com.intellij.openapi.diagnostic.Logger.getInstance(clazz))
    }

}

private class LoggerWrapper(private val instance: com.intellij.openapi.diagnostic.Logger) : Logger {
    override fun doLogDebug(message: String?, t: Throwable?) {
        instance.debug(message, t)
    }

    override fun doLogInfo(message: String?, t: Throwable?) {
        instance.info(message, t)
    }

    override fun doLogWarn(message: String?, t: Throwable?) {
        instance.warn(message, t)
    }

    override fun doLogError(message: String?, t: Throwable?) {
        instance.error(message, t)
    }

}