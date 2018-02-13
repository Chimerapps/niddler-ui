package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.NiddlerClient
import com.icapps.niddler.ui.NiddlerClientDebuggerInterface
import com.icapps.niddler.ui.adb.ADBBootstrap
import com.icapps.niddler.ui.codegen.CurlCodeGenerator
import com.icapps.niddler.ui.connection.NiddlerMessageListener
import com.icapps.niddler.ui.debugger.DebuggingSession
import com.icapps.niddler.ui.debugger.model.ConcreteDebuggingSession
import com.icapps.niddler.ui.debugger.model.DebuggerService
import com.icapps.niddler.ui.debugger.model.ServerDebuggerInterface
import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfiguration
import com.icapps.niddler.ui.export.HarExport
import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.form.ui.NiddlerUserInterface
import com.icapps.niddler.ui.model.*
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
import com.icapps.niddler.ui.model.ui.*
import com.icapps.niddler.ui.setColumnFixedWidth
import com.icapps.niddler.ui.setColumnMinWidth
import com.icapps.niddler.ui.util.ClipboardUtil
import com.icapps.niddler.ui.util.WideSelectionTreeUI
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.io.File
import java.net.URI
import javax.swing.*

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class NiddlerWindow(private val windowContents: NiddlerUserInterface, private val sdkPathGuesses: Collection<String>)
    : NiddlerMessageListener, ParsedNiddlerMessageListener, NiddlerMessagePopupMenu.Listener,
        NiddlerMainToolbar.ToolbarListener {

    private val messages = MessageContainer(NiddlerMessageBodyParser())
    private val messagePopupMenu = NiddlerMessagePopupMenu(this)

    private lateinit var adbConnection: ADBBootstrap
    private var messageMode = MessageMode.TIMELINE
    private var currentFilter: String = ""
    private var debuggerSession: DebuggingSession? = null
    private var currentDebuggerConfiguration: DebuggerConfiguration?=null

    fun init() {
        windowContents.init(messages)
        adbConnection = ADBBootstrap(sdkPathGuesses)

        windowContents.overview.messagesAsTable.apply {
            //TODO cleanup
            componentPopupMenu = messagePopupMenu
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
            componentPopupMenu = messagePopupMenu
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

        windowContents.setStatusText("<>")

        windowContents.toolbar.listener = this

        windowContents.detail.message = null

        windowContents.connectButtonListener = {
            val selection = NiddlerConnectDialog.showDialog(SwingUtilities.getWindowAncestor(windowContents.asComponent), adbConnection.bootStrap(), null, null)
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
        val ip = if (params.serial != null) {
            adbConnection.extend(params.serial)?.forwardTCPPort(6555, params.port)
            "127.0.0.1"
        } else
            params.ip!!

        initNiddlerOnDevice(ip)
    }

    private var niddlerClient: NiddlerClient? = null

    private fun disconnect() {
        niddlerClient?.close()
        niddlerClient?.unregisterMessageListener(this)
        niddlerClient?.unregisterMessageListener(messages)
        if (niddlerClient != null) {
            //TODO Remove previous port mapping, this could cause conflicts, to check
        }
        onClosed()
    }

    private fun initNiddlerOnDevice(ip: String) {
        disconnect()
        messages.clear()

        val tempUri = URI.create("sis://$ip")
        val port = if (tempUri.port == -1) 6555 else tempUri.port

        niddlerClient = NiddlerClient(URI.create("ws://${tempUri.host}:$port"))
        niddlerClient?.registerMessageListener(this)
        niddlerClient?.registerMessageListener(messages)
        niddlerClient?.connectBlocking()
    }

    override fun onReady() {
        MainThreadDispatcher.dispatch {
            windowContents.setStatusText("Connected")
            windowContents.setStatusIcon(ImageIcon(NiddlerWindow::class.java.getResource("/ic_connected.png")))
            windowContents.disconnectButton.isEnabled = true
        }
    }

    override fun onClosed() {
        MainThreadDispatcher.dispatch {
            windowContents.setStatusText("Disconnected")
            windowContents.setStatusIcon(ImageIcon(NiddlerWindow::class.java.getResource("/ic_disconnected.png")))
            windowContents.disconnectButton.isEnabled = false
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
        (windowContents.overview.messagesAsTable.model as? MessagesModel)?.updateMessages(messages)
    }

    override fun onLinkedSelected() {
        windowContents.overview.showLinked()
        messageMode = MessageMode.LINKED
        (windowContents.overview.messagesAsTree.model as? MessagesModel)?.updateMessages(messages)
    }

    override fun onClearSelected() {
        messages.clear()
        if (messageMode == MessageMode.TIMELINE)
            (windowContents.overview.messagesAsTable.model as? MessagesModel)?.updateMessages(messages)
        else
            (windowContents.overview.messagesAsTree.model as? MessagesModel)?.updateMessages(messages)

        windowContents.overview.messagesAsTable.clearSelection()
        windowContents.overview.messagesAsTree.clearSelection()
        checkRowSelectionState()
    }

    override fun onConfigureBreakpointsSelected() {
        val parent = SwingUtilities.getWindowAncestor(windowContents.asComponent)
        val dialog = windowContents.componentsFactory
                .createDebugConfigurationDialog(parent, windowContents.componentsFactory.loadSavedConfiguration())
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
                (windowContents.overview.messagesAsTable.model as? MessagesModel)?.updateMessages(messages.filtered(currentFilter))
                if (previousSelection != -1) {
                    try {
                        windowContents.overview.messagesAsTable.addRowSelectionInterval(previousSelection, previousSelection)
                    } catch (ignored: IllegalArgumentException) {
                    }
                }
            } else {
                val previousSelection = windowContents.overview.messagesAsTree.selectionPath
                (windowContents.overview.messagesAsTree.model as? MessagesModel)?.updateMessages(messages.filtered(currentFilter))
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
            windowContents.setStatusText("Connected to ${serverInfo.serverName} (${serverInfo.serverDescription})")
        }
        if (serverInfo.protocol >= 1) { //TODO constant
            val debuggerInterface = ServerDebuggerInterface(DebuggerService(NiddlerClientDebuggerInterface(niddlerClient!!)))
            debuggerSession = ConcreteDebuggingSession(debuggerInterface)
            currentDebuggerConfiguration?.let { debuggerSession?.applyConfiguration(it) }
        }
    }

    override fun onCopyUrlClicked() {
        val message = windowContents.detail.message ?: return
        message.let {
            if (message.isRequest) {
                ClipboardUtil.copyToClipboard(StringSelection(message.url))
            } else {
                ClipboardUtil.copyToClipboard(StringSelection(messages.findRequest(message)?.url))
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
                JOptionPane.showConfirmDialog(null, "Binary data cannot be copied to clipboard.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE)
            }
            else -> {
            } //Nothing to copy
        }
        clipboardData?.let { ClipboardUtil.copyToClipboard(it) }
    }

    override fun onExportCurlRequestClicked() {
        val message = windowContents.detail.message ?: return
        val request = messages.findRequest(message)
        if (request == null) {
            JOptionPane.showConfirmDialog(null, "Could not find request.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE)
            return
        }
        ClipboardUtil.copyToClipboard(StringSelection(CurlCodeGenerator().generateRequestCode(request)))
    }

    private fun showExportDialog() {
        var exportLocation = windowContents.componentsFactory.showSaveDialog("Save export to", ".har") ?: return
        if (!exportLocation.endsWith(".har"))
            exportLocation += ".har"
        HarExport(File(exportLocation)).export(messages.getMessagesLinked())
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateMessages()
    }
}