package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.NiddlerClient
import com.icapps.niddler.ui.adb.ADBBootstrap
import com.icapps.niddler.ui.connection.NiddlerMessageListener
import com.icapps.niddler.ui.export.HarExport
import com.icapps.niddler.ui.form.components.NiddlerToolbar
import com.icapps.niddler.ui.model.*
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
import com.icapps.niddler.ui.model.ui.*
import com.icapps.niddler.ui.setColumnFixedWidth
import com.icapps.niddler.ui.setColumnMinWidth
import com.icapps.niddler.ui.util.WideSelectionTreeUI
import java.awt.BorderLayout
import java.io.File
import java.net.URI
import javax.swing.*

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class NiddlerWindow(private val windowContents: NiddlerUserInterface, private val sdkPathGuesses: Collection<String>)
    : JPanel(BorderLayout()), NiddlerMessageListener, ParsedNiddlerMessageListener, NiddlerMessagePopupMenu.Listener,
        NiddlerToolbar.ToolbarListener {

    private val messages = MessageContainer(NiddlerMessageBodyParser())
    //private val detailContainer = MessageDetailContainer(interfaceFactory, messages)
    private val messagePopupMenu = NiddlerMessagePopupMenu(this)

    private lateinit var adbConnection: ADBBootstrap
    private var messageMode = MessageMode.TIMELINE

    fun init() {
        windowContents.init()
        adbConnection = ADBBootstrap(sdkPathGuesses)
        add(windowContents.asComponent, BorderLayout.CENTER) //TODO remove?

        //windowContents.splitPane.right = detailContainer.asComponent

        windowContents.messagesAsTable.apply {
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
                        if (windowContents.messagesAsTable.selectedRowCount == 0) {
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
        windowContents.messagesAsTree.apply {
            componentPopupMenu = messagePopupMenu
            model = LinkedMessagesModel()
            ui = WideSelectionTreeUI()

            selectionModel.addTreeSelectionListener {
                MainThreadDispatcher.dispatch {
                    if (messageMode == MessageMode.LINKED) {
                        if (windowContents.messagesAsTree.selectionCount == 0) {
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

        //detailContainer.clear()

        windowContents.connectButtonListener = {
            val selection = NiddlerConnectDialog.showDialog(SwingUtilities.getWindowAncestor(this), adbConnection.bootStrap(), null, null)
            if (selection != null)
                onDeviceSelectionChanged(selection)
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
            if (windowContents.messagesAsTable.selectedRowCount == 0) {
                //detailContainer.clear()
            } else {
                val selectedRow = windowContents.messagesAsTable.selectedRow
                val model = windowContents.messagesAsTable.model
                if (model is TimelineMessagesTableModel) {
                    val row = model.getRow(selectedRow)
                    //detailContainer.setMessage(row)
                }
            }
        } else if (messageMode == MessageMode.LINKED) {
            if (windowContents.messagesAsTree.selectionCount == 0) {
                //detailContainer.clear()
            } else {
                val selectedPath = windowContents.messagesAsTree.selectionPath?.lastPathComponent
                if (selectedPath is RequestNode) {
                    val request = selectedPath.request
                    if (request == null) {
                        // detailContainer.clear()
                    } else {
                        //   detailContainer.setMessage(request)
                    }
                } else if (selectedPath is ResponseNode) {
                    //detailContainer.setMessage(selectedPath.message)
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

    private fun initNiddlerOnDevice(ip: String) {
        niddlerClient?.close()
        niddlerClient?.unregisterMessageListener(this)
        niddlerClient?.unregisterMessageListener(messages)
        messages.clear()
        if (niddlerClient != null) {
            //TODO Remove previous port mapping, this could cause conflicts, to check
        }
        niddlerClient = NiddlerClient(URI.create("ws://$ip:6555"))
        niddlerClient?.registerMessageListener(this)
        niddlerClient?.registerMessageListener(messages)
        niddlerClient?.connectBlocking()
    }

    override fun onReady() {
        MainThreadDispatcher.dispatch {
            windowContents.setStatusText("Connected")
            windowContents.setStatusIcon(ImageIcon(NiddlerWindow::class.java.getResource("/ic_connected.png")))
        }
    }

    override fun onClosed() {
        MainThreadDispatcher.dispatch {
            windowContents.setStatusText("Disconnected")
            windowContents.setStatusIcon(ImageIcon(NiddlerWindow::class.java.getResource("/ic_disconnected.png")))
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
        //windowContents.messagesScroller.setViewportView(windowContents.messagesAsTable)
        messageMode = MessageMode.TIMELINE
        (windowContents.messagesAsTable.model as? MessagesModel)?.updateMessages(messages)
    }

    override fun onLinkedSelected() {
        //windowContents.messagesScroller.setViewportView(windowContents.messagesAsTree)
        messageMode = MessageMode.LINKED
        (windowContents.messagesAsTree.model as? MessagesModel)?.updateMessages(messages)
    }

    override fun onClearSelected() {
        messages.clear()
        if (messageMode == MessageMode.TIMELINE)
            (windowContents.messagesAsTable.model as? MessagesModel)?.updateMessages(messages)
        else
            (windowContents.messagesAsTree.model as? MessagesModel)?.updateMessages(messages)

        windowContents.messagesAsTable.clearSelection()
        windowContents.messagesAsTree.clearSelection()
        checkRowSelectionState()
    }

    override fun onExportSelected() {
        showExportDialog()
    }

    private fun updateMessages() {
        MainThreadDispatcher.dispatch {

            if (messageMode == MessageMode.TIMELINE) {
                val previousSelection = windowContents.messagesAsTable.selectedRow
                (windowContents.messagesAsTable.model as? MessagesModel)?.updateMessages(messages)
                if (previousSelection != -1) {
                    try {
                        windowContents.messagesAsTable.addRowSelectionInterval(previousSelection, previousSelection)
                    } catch (ignored: IllegalArgumentException) {
                    }
                }
            } else {
                (windowContents.messagesAsTree.model as? MessagesModel)?.updateMessages(messages)
            }
        }
    }

    override fun onServerInfo(serverInfo: NiddlerServerInfo) {
        MainThreadDispatcher.dispatch {
            //windowContents.updateProtocol(serverInfo.protocol)
            windowContents.setStatusText("Connected to ${serverInfo.serverName} (${serverInfo.serverDescription})")
        }
    }

    override fun onCopyUrlClicked() {
//        val message = detailContainer.getMessage()
//        message?.let {
//            if (message.isRequest) {
//                ClipboardUtil.copyToClipboard(StringSelection(message.url))
//            } else {
//                ClipboardUtil.copyToClipboard(StringSelection(messages.findRequest(message)?.url))
//            }
//        }
    }

    override fun onCopyBodyClicked() {
//        val message = detailContainer.getMessage() ?: return
//
//        var clipboardData: Transferable? = null
//        when (message.bodyFormat.type) {
//            BodyFormatType.FORMAT_IMAGE -> {
//                clipboardData = ClipboardUtil.imageTransferableFromBytes(message.message.getBodyAsBytes)
//            }
//            BodyFormatType.FORMAT_PLAIN,
//            BodyFormatType.FORMAT_JSON,
//            BodyFormatType.FORMAT_HTML,
//            BodyFormatType.FORMAT_FORM_ENCODED,
//            BodyFormatType.FORMAT_XML -> {
//                clipboardData = StringSelection(message.message.getBodyAsString(message.bodyFormat.encoding))
//            }
//            BodyFormatType.FORMAT_BINARY -> {
//                JOptionPane.showConfirmDialog(null, "Binary data cannot be copied to clipboard.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE)
//            }
//            else -> {
//            } //Nothing to copy
//        }
//        clipboardData?.let { ClipboardUtil.copyToClipboard(it) }
    }

    override fun onExportCurlRequestClicked() {
//        val message = detailContainer.getMessage() ?: return
//        val request = messages.findRequest(message)
//        if (request == null) {
//            JOptionPane.showConfirmDialog(null, "Could not find request.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE)
//            return
//        }
//        ClipboardUtil.copyToClipboard(StringSelection(CurlCodeGenerator().generateRequestCode(request)))
    }

    private fun showExportDialog() {
        var exportLocation = windowContents.componentsFactory.showSaveDialog("Save export to", ".har") ?: return
        if (!exportLocation.endsWith(".har"))
            exportLocation += ".har"
        HarExport(File(exportLocation)).export(messages.getMessagesLinked())
    }
}