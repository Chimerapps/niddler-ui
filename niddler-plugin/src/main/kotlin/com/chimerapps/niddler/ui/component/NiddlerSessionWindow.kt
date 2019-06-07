package com.chimerapps.niddler.ui.component

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.actions.ConnectAction
import com.chimerapps.niddler.ui.actions.DisconnectAction
import com.chimerapps.niddler.ui.actions.LinkedAction
import com.chimerapps.niddler.ui.actions.ScrollToBottomAction
import com.chimerapps.niddler.ui.actions.SimpleAction
import com.chimerapps.niddler.ui.actions.TimelineAction
import com.chimerapps.niddler.ui.component.view.MessageDetailView
import com.chimerapps.niddler.ui.component.view.MessagesView
import com.chimerapps.niddler.ui.component.view.NiddlerStatusBar
import com.chimerapps.niddler.ui.component.view.TimelineView
import com.chimerapps.niddler.ui.model.AppPreferences
import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.device.DirectPreparedConnection
import com.icapps.niddler.lib.device.PreparedDeviceConnection
import com.icapps.niddler.lib.model.InMemoryNiddlerMessageStorage
import com.icapps.niddler.lib.model.NiddlerMessageBodyParser
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.model.ParsedNiddlerMessageListener
import com.icapps.niddler.lib.model.classifier.HeaderBodyClassifier
import com.icapps.niddler.lib.utils.freePort
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.JBSplitter
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class NiddlerSessionWindow(private val niddlerToolWindow: NiddlerToolWindow) : JPanel(BorderLayout()), ParsedNiddlerMessageListener<ParsedNiddlerMessage> {

    private companion object {
        private const val APP_PREFERENCE_SCROLL_TO_END = "scrollToEnd"
        private const val APP_PREFERENCE_SPLITTER_STATE = "${AppPreferences.NIDDLER_PREFIX}detailSplitter"
    }

    private val rootContent = JPanel(BorderLayout())
    private val connectToolbar = setupConnectToolbar()
    private val viewToolbar = setupViewToolbar()
    private val statusBar = NiddlerStatusBar()
    private val splitter = JBSplitter(APP_PREFERENCE_SPLITTER_STATE, 0.6f)
    private val detailView = MessageDetailView()

    var currentViewMode: ViewMode = ViewMode.VIEW_MODE_TIMELINE
        set(value) {
            if (field == value)
                return

            field = value
            updateView()
            viewToolbar.updateActionsImmediately()
        }
    var connectionMode: ConnectionMode = ConnectionMode.MODE_DISCONNECTED
        private set(value) {
            field = value
            connectToolbar.updateActionsImmediately()
        }
    var scrollToEnd: Boolean = AppPreferences.get(APP_PREFERENCE_SCROLL_TO_END, default = true)
        set(value) {
            if (field == value)
                return

            currentMessagesView?.updateScrollToEnd(value)
            AppPreferences.put(APP_PREFERENCE_SCROLL_TO_END, value, default = true)
            field = value
            viewToolbar.updateActionsImmediately()
        }

    private var currentMessagesView: MessagesView? = null
    private val bodyParser = NiddlerMessageBodyParser(HeaderBodyClassifier(emptyList())) //TODO extensions!
    private val messageContainer = NiddlerMessageContainer(bodyParser::parseBody, InMemoryNiddlerMessageStorage())
    private var niddlerClient: NiddlerClient? = null
    private var lastConnection: PreparedDeviceConnection? = null

    init {
        add(rootContent, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)
        updateView()

        splitter.secondComponent = detailView

        rootContent.add(splitter, BorderLayout.CENTER)
        messageContainer.registerListener(this)
    }

    fun currentViewModeUnselected() {
        when (currentViewMode) {
            ViewMode.VIEW_MODE_TIMELINE -> currentViewMode = ViewMode.VIEW_MODE_LINKED
            ViewMode.VIEW_MODE_LINKED -> currentViewMode = ViewMode.VIEW_MODE_TIMELINE
        }
    }

    fun onClosed() {
        niddlerClient?.let { messageContainer.detach(it) }
        niddlerClient?.close()
        niddlerClient = null
        lastConnection?.tearDown()
        lastConnection = null

        messageContainer.storage.clear()
        messageContainer.unregisterListener(this)
    }

    private fun setupConnectToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        actionGroup.add(ConnectAction(this) {
            val result = ConnectDialog.show(SwingUtilities.getWindowAncestor(this), niddlerToolWindow.adbInterface ?: return@ConnectAction) ?: return@ConnectAction

            result.discovered?.let {
                tryConnectSession(it)
            }
            result.direct?.let {
                tryConnectDirect(it)
            }
        })
        actionGroup.add(DisconnectAction(this) {
            niddlerClient?.close()
            niddlerClient = null
            lastConnection?.tearDown()
            lastConnection = null

            connectionMode = ConnectionMode.MODE_DISCONNECTED
        })

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, true)
        rootContent.add(toolbar.component, BorderLayout.NORTH)
        return toolbar
    }

    private fun setupViewToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        val timelineAction = TimelineAction(window = this)
        actionGroup.add(timelineAction)

        val linkedAction = LinkedAction(window = this)
        actionGroup.add(linkedAction)

        actionGroup.addSeparator()
        actionGroup.add(ScrollToBottomAction(window = this))
        actionGroup.addSeparator()

        actionGroup.add(SimpleAction("Clear local", "Remove locally cached messages", icon = AllIcons.Actions.GC) {
            messageContainer.storage.clear()
            currentMessagesView?.onMessagesUpdated()
        })


        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, false)
        add(toolbar.component, BorderLayout.WEST)
        return toolbar
    }

    private fun updateView() {
        when (currentViewMode) {
            ViewMode.VIEW_MODE_TIMELINE -> replaceMessagesView(TimelineView(messageContainer.storage))
            ViewMode.VIEW_MODE_LINKED -> TODO()
        }
    }

    private fun <T> replaceMessagesView(messagesView: T) where T : JComponent, T : MessagesView {
        splitter.firstComponent = messagesView

        messagesView.updateScrollToEnd(scrollToEnd)
        currentMessagesView = messagesView
    }

    private fun tryConnectDirect(directConnection: ManualConnection) {
        niddlerClient?.close()
        niddlerClient = null
        lastConnection?.tearDown()
        lastConnection = null

        connectOnConnection(DirectPreparedConnection(directConnection.ip, directConnection.port))
    }

    private fun tryConnectSession(discovered: DiscoveredDeviceConnection) {
        niddlerClient?.close()
        niddlerClient = null
        lastConnection?.tearDown()
        lastConnection = null

        val connection = discovered.device.prepareConnection(freePort(), discovered.session.port)
        connectOnConnection(connection)
    }

    private fun connectOnConnection(connection: PreparedDeviceConnection) {
        niddlerClient = NiddlerClient(connection.uri, withDebugger = false).also {
            messageContainer.attach(it)
            it.registerMessageListener(statusBar)
        }
        niddlerClient?.connect()
        lastConnection = connection
        connectionMode = ConnectionMode.MODE_CONNECTED
    }

    override fun onMessage(message: ParsedNiddlerMessage) {
        currentMessagesView?.onMessagesUpdated()
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