package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.NiddlerClient
import com.icapps.niddler.ui.adb.ADBBootstrap
import com.icapps.niddler.ui.connection.NiddlerMessageListener
import com.icapps.niddler.ui.model.*
import com.icapps.niddler.ui.model.messages.NiddlerServerInfo
import com.icapps.niddler.ui.model.ui.TimelineMessagesTableModel
import com.icapps.niddler.ui.setColumnFixedWidth
import com.icapps.niddler.ui.util.ClipboardUtil
import java.awt.BorderLayout
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.net.URI
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class NiddlerWindow(interfaceFactory: InterfaceFactory) : JPanel(BorderLayout()), NiddlerMessageListener, ParsedNiddlerMessageListener, NiddlerMessagePopupMenu.Listener {

    private val messages = MessageContainer(NiddlerMessageBodyParser())
    private val detailContainer = MessageDetailContainer(interfaceFactory, messages)
    private val windowContents = NiddlerUIContainer(interfaceFactory)
    private val messagePopupMenu = NiddlerMessagePopupMenu(this)

    private lateinit var adbConnection: ADBBootstrap

    fun init() {
        try {
            adbConnection = ADBBootstrap()
        } catch(e: IllegalStateException) {
            add(JLabel("Could not find ADB. Is adb accessible from your path or did you define the ANDROID_HOME environment variable"), BorderLayout.CENTER)
            return
        }
        add(windowContents.rootPanel, BorderLayout.CENTER)

        windowContents.splitPane.right = detailContainer.asComponent

        windowContents.messages.apply {
            componentPopupMenu = messagePopupMenu
            model = TimelineMessagesTableModel()
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setColumnFixedWidth(0, 90)
            setColumnFixedWidth(1, 36)
            setColumnFixedWidth(2, 70)
            setColumnFixedWidth(3, 400)
            tableHeader = null

            selectionModel.addListSelectionListener {
                MainThreadDispatcher.dispatch {
                    if (windowContents.messages.selectedRowCount == 0) {
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

        windowContents.statusText.text = "<>"
        windowContents.statusBar.border = BorderFactory.createCompoundBorder(windowContents.statusBar.border, EmptyBorder(1, 6, 1, 6))

        detailContainer.clear()
        windowContents.buttonClear.addActionListener {
            messages.clear()
            val model = windowContents.messages.model as TimelineMessagesTableModel
            windowContents.messages.clearSelection()
            checkRowSelectionState()
            model.updateMessages(messages)
        }

        windowContents.connectButton.addActionListener {
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
        if (windowContents.messages.selectedRowCount == 0) {
            detailContainer.clear()
        } else {
            val selectedRow = windowContents.messages.selectedRow
            val row = (windowContents.messages.model as TimelineMessagesTableModel).getRow(selectedRow)
            detailContainer.setMessage(row)
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
            windowContents.statusText.text = "Connected"
            windowContents.statusText.icon = ImageIcon(NiddlerWindow::class.java.getResource("/ic_connected.png"))
        }
    }

    override fun onClosed() {
        MainThreadDispatcher.dispatch {
            windowContents.statusText.text = "Disconnected"
            windowContents.statusText.icon = ImageIcon(NiddlerWindow::class.java.getResource("/ic_disconnected.png"))
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

    private fun updateMessages() {
        MainThreadDispatcher.dispatch {
            val previousSelection = windowContents.messages.selectedRow
            (windowContents.messages.model as TimelineMessagesTableModel).updateMessages(messages)
            if (previousSelection != -1) {
                try {
                    windowContents.messages.addRowSelectionInterval(previousSelection, previousSelection)
                } catch(ignored: IllegalArgumentException) {
                }
            }
        }
    }

    override fun onServerInfo(serverInfo: NiddlerServerInfo) {
        MainThreadDispatcher.dispatch {
            windowContents.statusText.text = "Connected to ${serverInfo.serverName} (${serverInfo.serverDescription})"
        }
    }

    override fun onCopyUrlClicked() {
        val message = detailContainer.getMessage()
        message?.let {
            if (message.isRequest) {
                ClipboardUtil.copyToClipboard(StringSelection(message.url))
            } else {
                ClipboardUtil.copyToClipboard(StringSelection(messages.findRequest(message)?.url))
            }
        }
    }

    override fun onCopyBodyClicked() {
        val message = detailContainer.getMessage() ?: return

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

}