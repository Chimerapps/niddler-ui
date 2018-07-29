package com.icapps.niddler.ui.form

import com.icapps.niddler.lib.connection.NiddlerClient
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.model.NiddlerServerInfo
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.icapps.niddler.lib.debugger.DebuggingSession
import com.icapps.niddler.lib.debugger.model.ConcreteDebuggingSession
import com.icapps.niddler.lib.debugger.model.DebuggerService
import com.icapps.niddler.lib.debugger.model.ServerDebuggerInterface
import com.icapps.niddler.lib.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.lib.device.Device
import com.icapps.niddler.lib.device.DirectPreparedConnection
import com.icapps.niddler.lib.device.NiddlerSession
import com.icapps.niddler.lib.device.PreparedDeviceConnection
import com.icapps.niddler.lib.device.adb.ADBBootstrap
import com.icapps.niddler.lib.device.local.LocalDevice
import com.icapps.niddler.lib.export.HarExport
import com.icapps.niddler.lib.model.InMemoryNiddlerMessageStorage
import com.icapps.niddler.lib.model.NiddlerMessageBodyParser
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.model.ParsedNiddlerMessageListener
import com.icapps.niddler.lib.model.SimpleUrlMatchFilter
import com.icapps.niddler.lib.model.classifier.BodyFormatType
import com.icapps.niddler.lib.model.classifier.HeaderBodyClassifier
import com.icapps.niddler.ui.codegen.CurlCodeGenerator
import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.form.ui.NiddlerUserInterface
import com.icapps.niddler.ui.model.MessageMode
import com.icapps.niddler.ui.model.ui.LinkedMessagesModel
import com.icapps.niddler.ui.model.ui.MessagesModel
import com.icapps.niddler.ui.model.ui.RequestNode
import com.icapps.niddler.ui.model.ui.ResponseNode
import com.icapps.niddler.ui.model.ui.TimelineMessagesTableModel
import com.icapps.niddler.ui.setColumnFixedWidth
import com.icapps.niddler.ui.setColumnMinWidth
import com.icapps.niddler.ui.util.ClipboardUtil
import com.icapps.niddler.ui.util.WideSelectionTreeUI
import com.icapps.niddler.ui.util.loadIcon
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.io.File
import javax.swing.JOptionPane
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.Timer

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class NiddlerWindow(private val windowContents: NiddlerUserInterface, private val sdkPathGuesses: Collection<String>)
    : NiddlerMessageListener, ParsedNiddlerMessageListener<ParsedNiddlerMessage>, NiddlerTableMessagePopupMenu.Listener,
        NiddlerMainToolbar.ToolbarListener {

    companion object {
        const val PROTCOL_VERSION_DEBUGGING = 4
    }

    private val bodyParser = NiddlerMessageBodyParser(HeaderBodyClassifier())
    private val messages = NiddlerMessageContainer(bodyParser::parseBody, InMemoryNiddlerMessageStorage())

    private lateinit var adbConnection: ADBBootstrap
    private var messageMode = MessageMode.TIMELINE
    private var currentFilter: String = ""
        set(value) {
            field = value
            if (field.isBlank())
                messages.storage.filter = null
            else
                messages.storage.filter = SimpleUrlMatchFilter(field)
            updateMessages()
        }
    private var debuggerSession: DebuggingSession? = null
    private var currentDebuggerConfiguration: DebuggerConfiguration? = null

    private var niddlerClient: NiddlerClient? = null

    fun init() {
        windowContents.init(messages)
        adbConnection = ADBBootstrap(sdkPathGuesses)

        windowContents.overview.messagesAsTable.apply {
            //TODO cleanup
            popup = NiddlerTableMessagePopupMenu(this@NiddlerWindow)
            model = TimelineMessagesTableModel()
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setColumnFixedWidth(0, 90)
            setColumnFixedWidth(1, 36)
            setColumnFixedWidth(2, 70)
            setColumnMinWidth(3, 400)
            tableHeader = null

            selectionModel.addListSelectionListener {
                MainThreadDispatcher.dispatch {
                    if (messageMode == MessageMode.TIMELINE) {
                        if (windowContents.overview.messagesAsTable.selectedRowCount == 0) {
                            val timer = Timer(200) {
                                checkRowSelectionState()
                            }
                            timer.isRepeats = false
                            timer.start()
                        } else {
                            checkRowSelectionState()
                        }
                    }
                }
            }
        }
        windowContents.overview.messagesAsTree.apply {
            componentPopupMenu = NiddlerMessagePopupMenu(this@NiddlerWindow)
            model = LinkedMessagesModel()
            ui = WideSelectionTreeUI()

            selectionModel.addTreeSelectionListener {
                MainThreadDispatcher.dispatch {
                    if (messageMode == MessageMode.LINKED) {
                        if (windowContents.overview.messagesAsTree.selectionCount == 0) {
                            val timer = Timer(200) {
                                checkRowSelectionState()
                            }
                            timer.isRepeats = false
                            timer.start()
                        } else {
                            checkRowSelectionState()
                        }
                    }
                }
            }
        }

        windowContents.toolbar.listener = this
        windowContents.disconnectButton.isEnabled = false
        windowContents.detail.message = null

        windowContents.connectButtonListener = {
            val selection = NiddlerConnectDialog.showDialog(SwingUtilities.getWindowAncestor(windowContents.asComponent),
                    adbConnection, LocalDevice(), null, null, withDebugger = false)
            if (selection != null)
                onDeviceSelectionChanged(selection)
        }
        windowContents.debugButtonListener = {
            val selection = NiddlerConnectDialog.showDialog(SwingUtilities.getWindowAncestor(windowContents.asComponent),
                    adbConnection, LocalDevice(), null, null, withDebugger = true)
            if (selection != null)
                onDeviceSelectionChanged(selection)
        }
        windowContents.disconnectButtonListener = {
            disconnect()
            windowContents.disconnectButton.isEnabled = false
        }

        windowContents.filterListener = {
            applyFilter(it ?: "")
        }
    }

    fun onWindowInvisible() {
        messages.unregisterListener(this@NiddlerWindow)
    }

    fun onWindowVisible() {
        messages.registerListener(this@NiddlerWindow)
        updateMessages()
    }

    private fun checkRowSelectionState() {
        if (messageMode == MessageMode.TIMELINE) {
            if (windowContents.overview.messagesAsTable.selectedRowCount == 0) {
                windowContents.detail.message = null
            } else {
                val selectedRow = windowContents.overview.messagesAsTable.selectedRow
                val model = windowContents.overview.messagesAsTable.model
                if (model is TimelineMessagesTableModel) {
                    val row = model.getRow(selectedRow)
                    windowContents.detail.message = row
                }
            }
        } else if (messageMode == MessageMode.LINKED) {
            if (windowContents.overview.messagesAsTree.selectionCount == 0) {
                windowContents.detail.message = null
            } else {
                val selectedPath = windowContents.overview.messagesAsTree.selectionPath?.lastPathComponent
                if (selectedPath is RequestNode) {
                    val request = selectedPath.request
                    if (request == null) {
                        windowContents.detail.message = null
                    } else {
                        windowContents.detail.message = request
                    }
                } else if (selectedPath is ResponseNode) {
                    windowContents.detail.message = selectedPath.message
                }
            }
        }
    }

    private fun onDeviceSelectionChanged(params: NiddlerConnectDialog.ConnectSelection) {
        when {
            params.session != null -> initNiddlerOnSession(params.session, params.withDebugger)
            params.device != null -> initNiddlerOnDevice(params.device, params.port, params.withDebugger)
            else -> initNiddlerOnConnection(DirectPreparedConnection(params.ip!!, params.port), params.withDebugger)
        }
    }

    private fun disconnect() {
        niddlerClient?.let { client ->
            client.close()
            client.unregisterMessageListener(this)
            messages.detach(client)
        }
        if (niddlerClient != null) {
            //TODO Remove previous port mapping, this could cause conflicts, to check
        }
        onClearSelected()
        onClosed()
    }

    private fun initNiddlerOnSession(session: NiddlerSession, withDebugger: Boolean) {
        initNiddlerOnDevice(session.device, session.port, withDebugger)
    }

    private fun initNiddlerOnDevice(device: Device, port: Int, withDebugger: Boolean) {
        val connection = device.prepareConnection(6555, port)
        initNiddlerOnConnection(connection, withDebugger)
    }

    private fun initNiddlerOnConnection(connection: PreparedDeviceConnection, withDebugger: Boolean) {
        disconnect()
        messages.storage.clear()

        val niddlerClient = NiddlerClient(connection.uri, withDebugger).apply {
            registerMessageListener(this@NiddlerWindow)
            messages.attach(this)
        }
        niddlerClient.debugListener = windowContents.debugView
        this.niddlerClient = niddlerClient
        niddlerClient.connect()
    }

    override fun onReady() {
        MainThreadDispatcher.dispatch {
            windowContents.statusBar.onConnected()
            windowContents.disconnectButton.isEnabled = true
        }
    }

    override fun onClosed() {
        MainThreadDispatcher.dispatch {
            windowContents.statusBar.onDisconnected()
            windowContents.disconnectButton.isEnabled = false
        }
    }

    override fun onDebuggerAttached() {
        MainThreadDispatcher.dispatch {
            windowContents.statusBar.onDebuggerAttached()
        }
    }

    override fun onDebuggerActive() {
        MainThreadDispatcher.dispatch {
            windowContents.statusBar.onDebuggerStatusChanged(active = true)
        }
    }

    override fun onDebuggerInactive() {
        MainThreadDispatcher.dispatch {
            windowContents.statusBar.onDebuggerStatusChanged(active = false)
        }
    }

    override fun onAuthRequest(): String? {
        return JOptionPane.showInputDialog("Enter the password")
    }

    override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
        // Nothing
    }

    override fun onMessage(message: ParsedNiddlerMessage) {
        updateMessages()
    }

    override fun onTimelineSelected() {
        windowContents.overview.showTable()
        messageMode = MessageMode.TIMELINE
        (windowContents.overview.messagesAsTable.model as? MessagesModel)?.updateMessages(messages.storage)
    }

    override fun onLinkedSelected() {
        windowContents.overview.showLinked()
        messageMode = MessageMode.LINKED
        (windowContents.overview.messagesAsTree.model as? MessagesModel)?.updateMessages(messages.storage)
    }

    override fun onDebuggerViewSelected() {
        windowContents.overview.showDebugView()
    }

    override fun onClearSelected() {
        messages.storage.clear()
        if (messageMode == MessageMode.TIMELINE)
            (windowContents.overview.messagesAsTable.model as? MessagesModel)?.updateMessages(messages.storage)
        else
            (windowContents.overview.messagesAsTree.model as? MessagesModel)?.updateMessages(messages.storage)

        windowContents.overview.messagesAsTable.clearSelection()
        windowContents.overview.messagesAsTree.clearSelection()
        checkRowSelectionState()
    }

    override fun onConfigureBreakpointsSelected() {
        val parent = SwingUtilities.getWindowAncestor(windowContents.asComponent)
        val dialog = windowContents.componentsFactory
                .createDebugConfigurationDialog(parent, currentDebuggerConfiguration
                        ?: windowContents.componentsFactory.loadSavedConfiguration())
        dialog.init { debuggerConfiguration ->
            windowContents.componentsFactory.saveConfiguration(debuggerConfiguration)

            currentDebuggerConfiguration = debuggerConfiguration
            debuggerSession?.applyConfiguration(debuggerConfiguration)
            //TODO send new config to connection
        }

        dialog.visibility = true
    }

    override fun onMuteBreakpointsSelected() {
        windowContents.toolbar.onBreakpointsMuted(true)
        //TODO
    }

    override fun onExportSelected() {
        showExportDialog()
    }

    private fun updateMessages() {
        MainThreadDispatcher.dispatch {

            if (messageMode == MessageMode.TIMELINE) {
                val previousSelection = windowContents.overview.messagesAsTable.selectedRow
                (windowContents.overview.messagesAsTable.model as? MessagesModel)?.updateMessages(messages.storage)
                if (previousSelection != -1) {
                    try {
                        windowContents.overview.messagesAsTable.addRowSelectionInterval(previousSelection,
                                previousSelection)
                    } catch (ignored: IllegalArgumentException) {
                    }
                }
            } else {
                val previousSelection = windowContents.overview.messagesAsTree.selectionPath
                (windowContents.overview.messagesAsTree.model as? MessagesModel)?.updateMessages(messages.storage)
                if (previousSelection != null) {
                    try {
                        windowContents.overview.messagesAsTree.selectionPath = previousSelection
                    } catch (ignored: IllegalArgumentException) {
                    }
                }
            }
        }
    }

    override fun onServerInfo(serverInfo: NiddlerServerInfo) {
        MainThreadDispatcher.dispatch {
            //TODO windowContents.updateProtocol(serverInfo.protocol)
            windowContents.statusBar.onApplicationInfo(serverInfo)
        }
        if (serverInfo.protocol >= PROTCOL_VERSION_DEBUGGING && niddlerClient!!.withDebugger) {
            val debuggerInterface = ServerDebuggerInterface(
                    DebuggerService(niddlerClient!!))
            debuggerInterface.connect()

            debuggerSession = ConcreteDebuggingSession(debuggerInterface)
            onDebuggerAttached()
            if (currentDebuggerConfiguration == null)
                currentDebuggerConfiguration = windowContents.componentsFactory.loadSavedConfiguration()
            currentDebuggerConfiguration?.let { debuggerSession?.applyConfiguration(it) }
            debuggerSession!!.startSession()
            onDebuggerActive()
        }
    }

    override fun onShowRelatedClicked(source: ParsedNiddlerMessage?) {
        if (source == null)
            return
        val toShow = if (source.isRequest)
            messages.storage.findResponse(source)
        else
            messages.storage.findRequest(source)

        windowContents.selectMessage(toShow ?: return)
    }

    override fun onCopyUrlClicked() {
        val message = windowContents.detail.message ?: return
        message.let {
            if (message.isRequest) {
                ClipboardUtil.copyToClipboard(StringSelection(message.url))
            } else {
                ClipboardUtil.copyToClipboard(StringSelection(messages.storage.findRequest(message)?.url))
            }
        }
    }

    override fun onCopyBodyClicked() {
        val message = windowContents.detail.message ?: return

        var clipboardData: Transferable? = null
        when (message.bodyFormat.type) {
            BodyFormatType.FORMAT_IMAGE -> {
                clipboardData = ClipboardUtil.imageTransferableFromBytes(message.message.getBodyAsBytes)
            }
            BodyFormatType.FORMAT_PLAIN,
            BodyFormatType.FORMAT_JSON,
            BodyFormatType.FORMAT_HTML,
            BodyFormatType.FORMAT_FORM_ENCODED,
            BodyFormatType.FORMAT_XML -> {
                clipboardData = StringSelection(message.message.getBodyAsString(message.bodyFormat.encoding))
            }
            BodyFormatType.FORMAT_BINARY -> {
                message.getBodyAsBytes?.let { data ->
                    val exportLocation = windowContents.componentsFactory.showSaveDialog("Save data to", "") ?: return
                    File(exportLocation).writeBytes(data)
                }
            }
            else -> {
            } //Nothing to copy
        }
        clipboardData?.let { ClipboardUtil.copyToClipboard(it) }
    }

    override fun onExportCurlRequestClicked() {
        val message = windowContents.detail.message ?: return
        val request = messages.storage.findRequest(message)
        if (request == null) {
            JOptionPane.showConfirmDialog(null, "Could not find request.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE)
            return
        }
        ClipboardUtil.copyToClipboard(StringSelection(CurlCodeGenerator().generateRequestCode(request)))
    }

    private fun showExportDialog() {
        var applyFilter = false
        if (!currentFilter.isBlank()) {
            val option = JOptionPane.showOptionDialog(windowContents.asComponent, "A filter is active.\nDo you wish to export only the items matching the filter?",
                    "Export options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    "/niddler_logo.png".loadIcon<NiddlerWindow>(),
                    arrayOf("Current view", "All"), "All")
            when (option) {
                0-> applyFilter = true
                -1 -> return
            }
        }
        var exportLocation = windowContents.componentsFactory.showSaveDialog("Save export to", ".har") ?: return
        if (!exportLocation.endsWith(".har"))
            exportLocation += ".har"
        HarExport<ParsedNiddlerMessage>(File(exportLocation), HeaderBodyClassifier())
                .export(messages.storage, if (applyFilter) messages.storage.filter else null)
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateMessages()
    }
}
