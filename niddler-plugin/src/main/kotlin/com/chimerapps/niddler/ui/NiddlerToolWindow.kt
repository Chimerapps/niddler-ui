package com.chimerapps.niddler.ui

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.discovery.device.adb.ADBInterface
import com.chimerapps.niddler.ui.actions.NewSessionAction
import com.chimerapps.niddler.ui.component.NiddlerSessionWindow
import com.chimerapps.niddler.ui.util.adb.ADBUtils
import com.chimerapps.niddler.ui.util.ui.dispatchMain
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JPanel

class NiddlerToolWindow(private val project: Project, private val disposable: Disposable) : SimpleToolWindowPanel(/* vertical */ false, /* borderless */ true) {

    private val tabsContainer: RunnerLayoutUi
    private var c = 0

    private val actionToolbar: ActionToolbar
    val isReady: Boolean
        get() = adbInterface != null

    private var adbBootstrap: ADBBootstrap
    var adbInterface: ADBInterface? = null
        get() = synchronized(this@NiddlerToolWindow) {
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
                (event.content.component as NiddlerSessionWindow).onClosed()
            }
        }, disposable)

        adbBootstrap = ADBBootstrap(ADBUtils.guessPaths(project))
        bootStrapADB()
    }

    private fun bootStrapADB() {
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
            dispatchMain {
                remove(loadingContent)
                setContent(tabsContainer.component)

                newSessionWindow() //Create first session window
                actionToolbar.updateActionsImmediately()
            }
        }, "ADB startup").start()
    }

    private fun newSessionWindow() {
        val sessionWindow = NiddlerSessionWindow(project, disposable, this)
        val content = tabsContainer.createContent("${c++}-contentId", sessionWindow, "Session $c", null, null)
        content.setPreferredFocusedComponent { sessionWindow }

        content.isCloseable = true
        tabsContainer.addContent(content, -1, PlaceInGrid.center, false)
    }

    private fun setupViewActions(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        val newSessionAction = NewSessionAction(this) {
            newSessionWindow()
        }
        actionGroup.add(newSessionAction)

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, false)
        setToolbar(toolbar.component)
        return toolbar
    }

}