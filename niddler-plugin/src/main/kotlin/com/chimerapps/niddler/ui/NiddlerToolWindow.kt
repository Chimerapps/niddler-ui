package com.chimerapps.niddler.ui

import com.chimerapps.discovery.device.Device
import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.discovery.device.debugbridge.DebugBridgeInterface
import com.chimerapps.discovery.device.sdb.SDBBootstrap
import com.chimerapps.niddler.ui.actions.ConfigureMapLocalAction
import com.chimerapps.niddler.ui.actions.ConfigureRewriteAction
import com.chimerapps.niddler.ui.actions.NewSessionAction
import com.chimerapps.niddler.ui.component.ConnectionMode
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.debugging.maplocal.MapLocalDialog
import com.chimerapps.niddler.ui.debugging.rewrite.RewriteConfig
import com.chimerapps.niddler.ui.debugging.rewrite.RewriteDialog
import com.chimerapps.niddler.ui.model.ProjectConfig
import com.chimerapps.niddler.ui.settings.NiddlerSettings
import com.chimerapps.niddler.ui.util.adb.ADBUtils
import com.chimerapps.niddler.ui.util.sdb.SDBUtils
import com.chimerapps.niddler.ui.util.ui.dispatchMain
import com.icapps.niddler.lib.debugger.model.maplocal.MapLocalConfiguration
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagLayout
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities

class NiddlerToolWindow(private val project: Project, private val disposable: Disposable) : SimpleToolWindowPanel(/* vertical */ false, /* borderless */ true) {

    companion object {
        fun get(project: Project): Pair<NiddlerToolWindow, ToolWindow>? {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val window = toolWindowManager.getToolWindow("Niddler") ?: return null
            val toolWindow = window.contentManager.getContent(0)?.component as? NiddlerToolWindow ?: return null
            return toolWindow to window
        }
    }

    private val tabsContainer: RunnerLayoutUi
    private var c = 0

    private val actionToolbar: ActionToolbar
    val isReady: Boolean
        get() = adbInterface != null

    //TODO move bootstrapping to full plugin start to ensure we have a connection beforehand
    private var adbBootstrap: ADBBootstrap
    private var sdbBootstrap: SDBBootstrap

    var adbInterface: DebugBridgeInterface? = null
        get() = synchronized(this@NiddlerToolWindow) {
            val result = field ?: return null
            if (!result.isRealConnection) {
                val path = NiddlerSettings.instance.state.adbPath
                if (path != null && File(path).let { it.exists() && it.canExecute() }) {
                    adbBootstrap = ADBBootstrap(ADBUtils.guessPaths(project)) { NiddlerSettings.instance.state.adbPath }
                    field = adbBootstrap.bootStrap()
                }
            }
            field
        }
        private set(value) {
            synchronized(this@NiddlerToolWindow) {
                field = value
            }
        }
    var sdbInterface: DebugBridgeInterface? = null
        get() = synchronized(this@NiddlerToolWindow) {
            val result = field ?: return null
            if (!result.isRealConnection) {
                val path = NiddlerSettings.instance.state.sdbPath
                if (path != null && File(path).let { it.exists() && it.canExecute() }) {
                    sdbBootstrap = SDBBootstrap(SDBUtils.guessPaths(project)) { NiddlerSettings.instance.state.sdbPath }
                    field = sdbBootstrap.bootStrap()
                }
            }
            field
        }
        private set(value) {
            synchronized(this@NiddlerToolWindow) {
                field = value
            }
        }

    init {
        actionToolbar = setupViewActions()

        tabsContainer = RunnerLayoutUi.Factory.getInstance(project).create("niddler-ui", "Detail tabs", "Some session name?", disposable)
        tabsContainer.addListener(object : ContentManagerListener {
            override fun contentAdded(event: ContentManagerEvent) {
            }

            override fun contentRemoveQuery(event: ContentManagerEvent) {
            }

            override fun selectionChanged(event: ContentManagerEvent) {
            }

            override fun contentRemoved(event: ContentManagerEvent) {
                (event.content.component as NiddlerSessionWindow).onWindowClosed()
            }
        }, disposable)

        adbBootstrap = ADBBootstrap(ADBUtils.guessPaths(project)) { NiddlerSettings.instance.state.adbPath }
        sdbBootstrap = SDBBootstrap(SDBUtils.guessPaths(project)) { NiddlerSettings.instance.state.sdbPath }
        bootStrapDebugBridges()
    }

    fun redoADBBootstrapBlocking() {
        adbInterface = adbBootstrap.bootStrap()
    }

    private fun bootStrapDebugBridges() {
        val loadingContent = JPanel(GridBagLayout())

        val labelAndLoading = JPanel(BorderLayout())

        labelAndLoading.add(JBLabel("Starting adb").also {
            it.font = it.font.deriveFont(50.0f)
            it.foreground = Color.lightGray
        }, BorderLayout.NORTH)
        labelAndLoading.add(AsyncProcessIcon.Big("ADBLoadingIndicator").also { it.border = BorderFactory.createEmptyBorder(10, 0, 0, 0) }, BorderLayout.CENTER)

        loadingContent.add(labelAndLoading)
        setContent(loadingContent)

        Thread({
            adbInterface = adbBootstrap.bootStrap()
            sdbInterface = sdbBootstrap.bootStrap()
            dispatchMain {
                remove(loadingContent)
                setContent(tabsContainer.component)

                if (tabsContainer.contents.isEmpty())
                    newSessionWindow() //Create first session window
                actionToolbar.updateActionsImmediately()
            }
        }, "Debug Bridge startup").start()
    }

    private fun newSessionWindow(): NiddlerSessionWindow {
        val sessionWindow = NiddlerSessionWindow(project, disposable, this)
        val content = tabsContainer.createContent("${c++}-contentId", sessionWindow, "Session $c", null, null)
        content.setPreferredFocusedComponent { sessionWindow }
        sessionWindow.content = content

        content.isCloseable = true
        tabsContainer.addContent(content, -1, PlaceInGrid.center, false)
        tabsContainer.selectAndFocus(content, true, true)
        return sessionWindow
    }

    private fun unusedOrNewSessionWindow(): NiddlerSessionWindow {
        return tabsContainer.contents.find { content ->
            (content.component as? NiddlerSessionWindow)?.connectionMode == ConnectionMode.MODE_DISCONNECTED
        }?.let { it.component as NiddlerSessionWindow } ?: newSessionWindow()
    }

    private fun setupViewActions(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        val newSessionAction = NewSessionAction(this) {
            newSessionWindow()
        }
        val configureDebuggerAction = ConfigureRewriteAction(this) {
            RewriteDialog.show(SwingUtilities.getWindowAncestor(this), project)?.let {
                ProjectConfig.save(project, ProjectConfig.CONFIG_REWRITE, it)
                applyRewriteConfig(it)
            }
        }
        val configureLocalMapping = ConfigureMapLocalAction(this) {
            MapLocalDialog.show(SwingUtilities.getWindowAncestor(this), project)?.let {
                ProjectConfig.save(project, ProjectConfig.CONFIG_MAPLOCAL, it)
                applyMapLocalConfig(it)
            }
        }
        actionGroup.add(newSessionAction)
        actionGroup.add(configureDebuggerAction)
        actionGroup.add(configureLocalMapping)

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, false)
        setToolbar(toolbar.component)
        return toolbar
    }

    //TODO optimize performance
    private fun applyRewriteConfig(config: RewriteConfig) {
        tabsContainer.contents.mapNotNull { content ->
            val sessionWindow = (content.component as? NiddlerSessionWindow) ?: return@mapNotNull null
            if (sessionWindow.connectionMode == ConnectionMode.MODE_CONNECTED)
                sessionWindow
            else
                null
        }.forEach { sessionWindow ->
            sessionWindow.debuggerService?.let { debuggerService ->
                try {
                    debuggerService.rewriteInterface.clearRuleSets()
                    if (config.allEnabled) {
                        config.sets.forEach { set -> debuggerService.rewriteInterface.addRuleSet(set) }
                    }
                } catch (e: Throwable) {
                }
            }
            sessionWindow.rewriteDebugListener.updateRuleSets(config.sets)
        }
    }

    private fun applyMapLocalConfig(config: MapLocalConfiguration) {
        tabsContainer.contents.mapNotNull { content ->
            val sessionWindow = (content.component as? NiddlerSessionWindow) ?: return@mapNotNull null
            if (sessionWindow.connectionMode == ConnectionMode.MODE_CONNECTED)
                sessionWindow
            else
                null
        }.forEach { sessionWindow ->
            sessionWindow.debuggerService?.let { debuggerService ->
                try {
                    debuggerService.mapLocalInterface.clearRuleSets()
                    if (config.enabled) {
                        config.mappings.forEach { set -> debuggerService.mapLocalInterface.addLocalMapping(set) }
                    }
                } catch (e: Throwable) {
                }
            }
            sessionWindow.mapLocalDebugListener.updateMapLocal(config.mappings)
        }
    }

    fun newSessionForTag(tag: String, withDebugger: Boolean) {
        newSessionWindow().connectToTag(tag, withDebugger = withDebugger)
    }

    fun newSessionFor(info: QuickConnectionInfo, reuse: Boolean, connectUsingDebugger: Boolean) {
        val window = if (reuse) unusedOrNewSessionWindow() else newSessionWindow()
        info.device?.let {
            window.connectToDevice(it, info.port, withDebugger = connectUsingDebugger)
            return
        }

        window.connectToTag(info.tag, withDebugger = connectUsingDebugger)
    }

    fun newSessionFor(info: PIDQuickConnectionInfo, reuse: Boolean, connectUsingDebugger: Boolean) {
        val window = if (reuse) unusedOrNewSessionWindow() else newSessionWindow()

        window.connectToProccessOn(info.deviceSerial, info.pid, withDebugger = connectUsingDebugger)
    }

}

data class QuickConnectionInfo(
    val port: Int,
    val tag: String,
    val device: Device? = null
)

data class PIDQuickConnectionInfo(
    val deviceSerial: String,
    val pid: Int,
)