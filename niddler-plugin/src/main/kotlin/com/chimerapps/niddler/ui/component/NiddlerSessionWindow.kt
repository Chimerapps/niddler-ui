package com.chimerapps.niddler.ui.component

import com.chimerapps.discovery.device.Device
import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.PreparedDeviceConnection
import com.chimerapps.discovery.device.idevice.IDeviceBootstrap
import com.chimerapps.discovery.ui.ConnectDialog
import com.chimerapps.discovery.ui.DiscoveredDeviceConnection
import com.chimerapps.discovery.ui.ManualConnection
import com.chimerapps.discovery.utils.freePort
import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.actions.ConnectAction
import com.chimerapps.niddler.ui.actions.ConnectDebuggerAction
import com.chimerapps.niddler.ui.actions.DisconnectAction
import com.chimerapps.niddler.ui.actions.ExportAction
import com.chimerapps.niddler.ui.actions.LinkedAction
import com.chimerapps.niddler.ui.actions.ScrollToBottomAction
import com.chimerapps.niddler.ui.actions.SimpleAction
import com.chimerapps.niddler.ui.actions.TimelineAction
import com.chimerapps.niddler.ui.component.view.BaseUrlHideListener
import com.chimerapps.niddler.ui.component.view.LinkedView
import com.chimerapps.niddler.ui.component.view.MessageDetailView
import com.chimerapps.niddler.ui.component.view.MessagesView
import com.chimerapps.niddler.ui.component.view.NiddlerStatusBar
import com.chimerapps.niddler.ui.component.view.TimelineView
import com.chimerapps.niddler.ui.debugging.rewrite.RewriteConfig
import com.chimerapps.niddler.ui.model.AppPreferences
import com.chimerapps.niddler.ui.model.ProjectConfig
import com.chimerapps.niddler.ui.settings.NiddlerSettings
import com.chimerapps.niddler.ui.util.session.SessionFinderUtil
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
import com.chimerapps.niddler.ui.util.ui.ProjectSessionIconProvider
import com.chimerapps.niddler.ui.util.ui.chooseSaveFile
import com.chimerapps.niddler.ui.util.ui.dispatchMain
import com.chimerapps.niddler.ui.util.ui.ensureMain
import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.icapps.niddler.lib.debugger.model.DebuggerService
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteDebugListener
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteDebuggerInterface
import com.icapps.niddler.lib.export.HarExport
import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.NiddlerMessageBodyParser
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.ParsedNiddlerMessageProvider
import com.icapps.niddler.lib.model.SimpleUrlMatchFilter
import com.icapps.niddler.lib.model.classifier.HeaderBodyClassifier
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import com.icapps.niddler.lib.model.storage.binary.QuickBinaryMessageStorage
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.ui.FilterComponent
import com.intellij.ui.JBSplitter
import com.intellij.ui.content.Content
import com.intellij.util.IconUtil
import java.awt.BorderLayout
import java.io.File
import java.io.FileOutputStream
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

class NiddlerSessionWindow(private val project: Project,
                           disposable: Disposable,
                           private val niddlerToolWindow: NiddlerToolWindow) : JPanel(BorderLayout()), NiddlerMessageListener, BaseUrlHideListener {

    private companion object {
        private const val APP_PREFERENCE_SCROLL_TO_END = "scrollToEnd"
        private const val APP_PREFERENCE_SPLITTER_STATE = "${AppPreferences.NIDDLER_PREFIX}detailSplitter"
    }

    lateinit var content: Content

    private val rootContent = JPanel(BorderLayout())
    private val connectToolbar = setupConnectToolbar()
    private val exportAction = ExportAction(::doExport)
    private val viewToolbar = setupViewToolbar()
    private val statusBar = NiddlerStatusBar()
    private val splitter = JBSplitter(APP_PREFERENCE_SPLITTER_STATE, 0.6f)
    private var baseUrlHider: BaseUrlHider? = null
    private var currentFilter: NiddlerMessageStorage.Filter? = null
    private val debugListener = RewriteDebugListener()
    private var debuggerService: DebuggerService? = null//TODO disconnect

    var currentViewMode: ViewMode = ViewMode.VIEW_MODE_TIMELINE
        set(value) {
            if (field == value)
                return

            field = value
            ensureMain {
                updateView()
                viewToolbar.updateActionsImmediately()
            }
        }
    var connectionMode: ConnectionMode = ConnectionMode.MODE_DISCONNECTED
        private set(value) {
            field = value
            ensureMain {
                connectToolbar.updateActionsImmediately()
            }
        }
    var scrollToEnd: Boolean = AppPreferences.get(APP_PREFERENCE_SCROLL_TO_END, default = true)
        set(value) {
            if (field == value)
                return

            ensureMain {
                currentMessagesView?.updateScrollToEnd(value)
                AppPreferences.put(APP_PREFERENCE_SCROLL_TO_END, value, default = true)
                field = value
                viewToolbar.updateActionsImmediately()
            }
        }

    private var currentMessagesView: MessagesView? = null
    private val bodyParser = NiddlerMessageBodyParser(HeaderBodyClassifier(emptyList())) //TODO extensions!

    private val messageContainer = NiddlerMessageContainer(QuickBinaryMessageStorage())
    private val parsedNiddlerMessageProvider = ParsedNiddlerMessageProvider({ dispatchMain(it) }, bodyParser, messageContainer)
    private var niddlerClient: NiddlerClient? = null
    private var lastConnection: PreparedDeviceConnection? = null
    private val detailView = MessageDetailView(project, disposable, parsedNiddlerMessageProvider, messageContainer)

    init {
        add(rootContent, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)
        updateView()

        splitter.secondComponent = detailView

        rootContent.add(splitter, BorderLayout.CENTER)
        messageContainer.registerListener(this)
    }

    fun currentViewModeUnselected() {
        //Do not switch to other view mode until other view mode is supported
        currentViewMode = if (currentViewMode == ViewMode.VIEW_MODE_LINKED) {
            ViewMode.VIEW_MODE_TIMELINE
        } else {
            ViewMode.VIEW_MODE_LINKED
        }
        viewToolbar.updateActionsImmediately() //Update ui
    }

    fun onWindowClosed() {
        niddlerClient?.let { messageContainer.detach(it) }
        try {
            niddlerClient?.close()
        } catch (ignore: Throwable) {
        }
        niddlerClient = null
        try {
            lastConnection?.tearDown()
        } catch (ignore: Throwable) {
        }
        lastConnection = null

        messageContainer.clear()
        messageContainer.close()

        messageContainer.unregisterListener(this)
    }

    private fun setupConnectToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        actionGroup.add(ConnectAction(this) {
            showConnectDialog(withDebugger = false)
        })
        actionGroup.add(ConnectDebuggerAction(this) {
            showConnectDialog(withDebugger = true)
        })
        actionGroup.add(DisconnectAction(this) {
            try {
                niddlerClient?.close()
            } catch (ignore: Throwable) {
            }
            niddlerClient = null
            try {
                lastConnection?.tearDown()
            } catch (ignore: Throwable) {
            }
            lastConnection = null

            connectionMode = ConnectionMode.MODE_DISCONNECTED
        })

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, true)
        val toolbarContainer = JPanel(BorderLayout())
        toolbarContainer.add(toolbar.component, BorderLayout.WEST)

        val filter = object : FilterComponent("niddler-filter", 10, true) {
            override fun filter() {
                val filter = filter.trim()
                val currentFilter: NiddlerMessageStorage.Filter? = if (filter.isNotEmpty())
                    SimpleUrlMatchFilter(filter)
                else
                    null
                this@NiddlerSessionWindow.currentFilter = currentFilter
                currentMessagesView?.filter = currentFilter
            }
        }
        filter.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)
        toolbarContainer.add(filter, BorderLayout.EAST)

        rootContent.add(toolbarContainer, BorderLayout.NORTH)
        return toolbar
    }

    private fun showConnectDialog(withDebugger: Boolean) {
        val result = ConnectDialog.show(SwingUtilities.getWindowAncestor(this),
                niddlerToolWindow.adbInterface ?: return,
                IDeviceBootstrap(File(NiddlerSettings.instance.iDeviceBinariesPath ?: "/usr/local/bin")),
                Device.NIDDLER_ANNOUNCEMENT_PORT,
                sessionIconProvider = ProjectSessionIconProvider.instance(project)) ?: return

        result.discovered?.let {
            tryConnectSession(it, withDebugger)
        }
        result.direct?.let {
            tryConnectDirect(it, withDebugger)
        }
    }

    private fun setupViewToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        actionGroup.add(TimelineAction(window = this))
        actionGroup.add(LinkedAction(window = this))

        actionGroup.addSeparator()
        actionGroup.add(ScrollToBottomAction(window = this))
        actionGroup.addSeparator()

        actionGroup.add(SimpleAction("Clear local", "Remove locally cached messages", icon = AllIcons.Actions.GC) {
            messageContainer.clear()
            exportAction.isEnabled = !messageContainer.isEmpty()
            viewToolbar.updateActionsImmediately()
            currentMessagesView?.onMessagesUpdated()
        })
        actionGroup.addSeparator()
        actionGroup.add(exportAction)

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, false)
        add(toolbar.component, BorderLayout.WEST)
        return toolbar
    }

    private fun updateView() {
        ensureMain {
            when (currentViewMode) {
                ViewMode.VIEW_MODE_TIMELINE -> replaceMessagesView(TimelineView(project, messageContainer, detailView,
                        parsedNiddlerMessageProvider, baseUrlHideListener = this))
                ViewMode.VIEW_MODE_LINKED -> replaceMessagesView(LinkedView(messageContainer, detailView, parsedNiddlerMessageProvider, baseUrlHideListener = this))
            }
        }
    }

    private fun <T> replaceMessagesView(messagesView: T) where T : JComponent, T : MessagesView {
        splitter.firstComponent = messagesView

        messagesView.filter = currentFilter
        messagesView.urlHider = baseUrlHider

        messagesView.updateScrollToEnd(scrollToEnd)
        currentMessagesView = messagesView
    }

    private fun tryConnectDirect(directConnection: ManualConnection, withDebugger: Boolean) {
        try {
            niddlerClient?.close()
        } catch (ignore: Throwable) {
        }
        niddlerClient = null
        try {
            lastConnection?.tearDown()
        } catch (ignore: Throwable) {
        }
        lastConnection = null

        connectOnConnection(DirectPreparedConnection(directConnection.ip, directConnection.port), withDebugger)
    }

    private fun tryConnectSession(discovered: DiscoveredDeviceConnection, withDebugger: Boolean) {
        try {
            niddlerClient?.close()
        } catch (ignore: Throwable) {
        }
        niddlerClient = null
        try {
            lastConnection?.tearDown()
        } catch (ignore: Throwable) {
        }
        lastConnection = null

        val connection = discovered.device.prepareConnection(freePort(), discovered.session.port)
        connectOnConnection(connection, withDebugger)
    }

    private fun connectOnConnection(connection: PreparedDeviceConnection, withDebugger: Boolean) {
        messageContainer.clear()

        niddlerClient = NiddlerClient(connection.uri, withDebugger = withDebugger, messageStorage = messageContainer).also {
            if (withDebugger) {
                it.debugListener = debugListener
            }
            messageContainer.attach(it)
            it.registerMessageListener(statusBar)
            it.registerMessageListener(object : NiddlerMessageListener {
                override fun onClosed() {
                    ensureMain {
                        connectionMode = ConnectionMode.MODE_DISCONNECTED
                        content.icon?.let { icon ->
                            content.icon = IconUtil.desaturate(icon)
                        }
                    }
                }
            })
            it.registerMessageListener(object : NiddlerMessageListener {
                override fun onServerInfo(serverInfo: NiddlerServerInfo) {
                    ensureMain {
                        val newIcon = serverInfo.icon?.let { iconString ->
                            ProjectSessionIconProvider.instance(project).iconForString(iconString)
                        }
                        content.icon = newIcon
                    }
                }
            })
            it.registerMessageListener(detailView)

            it.registerMessageListener(object : NiddlerMessageListener {
                override fun onReady() {
                    val client = niddlerClient
                    if (client?.withDebugger == true) {
                        val rewriteConfig = ProjectConfig.load<RewriteConfig>(project, ProjectConfig.CONFIG_REWRITE) ?: return
                        debuggerService = DebuggerService(client).also { service ->
                            service.connect()
                            if (rewriteConfig.allEnabled) {
                                RewriteDebuggerInterface(service).also { rewriteDebuggerInterface ->
                                    debugListener.updateRuleSets(rewriteConfig.sets)
                                    rewriteConfig.sets.forEach { set -> rewriteDebuggerInterface.addRuleSet(set) }
                                }
                            }
                            service.setActive(true)
                        }
                    }
                }
            })
        }
        niddlerClient?.connect()
        lastConnection = connection
        connectionMode = ConnectionMode.MODE_CONNECTED
    }


    override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
        currentMessagesView?.onMessagesUpdated()

        val canExport = !messageContainer.isEmpty()
        if (exportAction.isEnabled != canExport) {
            exportAction.isEnabled = canExport
            dispatchMain(viewToolbar::updateActionsImmediately)
        }
    }

    override fun hideBaseUrl(baseUrl: String) {
        val hider = if (baseUrlHider == null) {
            val newHider = BaseUrlHider()
            baseUrlHider = newHider
            newHider
        } else {
            baseUrlHider!!
        }
        hider.hideBaseUrl(baseUrl)
        currentMessagesView?.urlHider = hider
    }

    override fun showBaseUrl(baseUrl: String) {
        val hider = baseUrlHider ?: return
        hider.unhideBaseUrl(baseUrl)
        if (!hider.hasHiddenBaseUrls)
            baseUrlHider = null
        currentMessagesView?.urlHider = baseUrlHider
    }

    private fun doExport() {
        val filter = currentFilter
        var applyFilter = false
        if (filter != null) {
            val option = JOptionPane.showOptionDialog(this, "A filter is active.\nDo you wish to export only the items matching the filter?",
                    "Export options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, IncludedIcons.Status.logo,
                    arrayOf("Current view", "All"), "All")
            when (option) {
                0 -> applyFilter = true
                -1 -> return
            }
        }
        val chosenFile = chooseSaveFile("Save export to", ".har") ?: return
        runWriteAction {
            val exportFile = if (chosenFile.extension.isEmpty()) {
                File(chosenFile.absolutePath + ".har")
            } else
                chosenFile

            HarExport(parsedNiddlerMessageProvider).export(FileOutputStream(exportFile), messageContainer, if (applyFilter) filter else null)
            NotificationUtil.info("Save complete", "<html>Export completed to <a href=\"file://${chosenFile.absolutePath}\">${chosenFile.name}</a></html>", project)
        }
    }

    fun connectToTag(tag: String, withDebugger: Boolean) {
        val adb = niddlerToolWindow.adbInterface
        val iDevice = IDeviceBootstrap(File(NiddlerSettings.instance.iDeviceBinariesPath ?: "/usr/local/bin"))
        val port = Device.NIDDLER_ANNOUNCEMENT_PORT
        object : SwingWorker<DiscoveredDeviceConnection?, Any>() {
            override fun doInBackground(): DiscoveredDeviceConnection? {
                return SessionFinderUtil(adb, iDevice, port).findSessionWithTag(tag)
            }

            override fun done() {
                val session = get() ?: return
                tryConnectSession(session, withDebugger = withDebugger)
            }
        }.execute()
    }

    fun connectToDevice(device: Device, port: Int, withDebugger: Boolean) {
        try {
            niddlerClient?.close()
        } catch (ignore: Throwable) {
        }
        niddlerClient = null
        try {
            lastConnection?.tearDown()
        } catch (ignore: Throwable) {
        }
        lastConnection = null

        val connection = device.prepareConnection(freePort(), port)
        connectOnConnection(connection, withDebugger)
    }
}

enum class ViewMode {
    VIEW_MODE_TIMELINE,
    VIEW_MODE_LINKED
}

enum class ConnectionMode {
    MODE_CONNECTED,
    MODE_DISCONNECTED
}