package com.icapps.niddler.ui.form.debug.view

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.MainThreadDispatcher
import com.icapps.niddler.ui.setColumnFixedWidth
import com.icapps.niddler.ui.util.loadIcon
import java.awt.BorderLayout
import java.awt.Component
import java.util.concurrent.CompletableFuture
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.Timer

/**
 * @author nicolaverbeeck
 */
class DebugView(private val componentsFactory: ComponentsFactory,
                private val messagesUpdateListener: (numMessagesWaiting: Int) -> Unit,
                private val messageContainer: NiddlerMessageContainer<ParsedNiddlerMessage>)
    : JPanel(BorderLayout()), NiddlerDebugListener {

    private companion object {
        private val logger = com.icapps.niddler.ui.util.logger<DebugView>()
    }

    private val waitingMessagesModel = MessagesModel(messagesUpdateListener)
    private val waitingMessagesList = JTable(waitingMessagesModel)
    private val detailView = DebugDetailView(componentsFactory)

    private val splitter = componentsFactory.createSplitPane()
    private val sendButton: Component
    private val cancelButton: Component

    init {
        waitingMessagesList.apply {
            fillsViewportHeight = false
            rowHeight = 32
            showHorizontalLines = true
            showVerticalLines = false

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setColumnFixedWidth(MessagesModel.COL_ICON, 36)
            setColumnFixedWidth(MessagesModel.COL_METHOD, 70)
            tableHeader = null

            selectionModel.addListSelectionListener {
                MainThreadDispatcher.dispatch {
                    if (waitingMessagesList.selectedRowCount == 0) {
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

        val toolbar = componentsFactory.createVerticalToolbar()
        sendButton = toolbar.addAction(loadIcon("/stepOut.png"), "Send to server") {
            onSubmitClicked()
        }
        cancelButton = toolbar.addAction(loadIcon("/cancel.png"), "Proceed without changes") {
            onCancelClicked()
        }
        sendButton.isEnabled = false
        cancelButton.isEnabled = false
        add(toolbar.component, BorderLayout.WEST)

        val scroller = componentsFactory.createScrollPane()
        scroller.setViewportView(waitingMessagesList)

        add(splitter.asComponent, BorderLayout.CENTER)
        splitter.left = scroller
        splitter.right = JScrollPane(detailView)
        splitter.resizePriority = .2
        splitter.asComponent.revalidate()
    }

    override fun onRequestOverride(message: NiddlerMessage): DebugRequest? {
        //TODO
        return null
    }

    override fun onRequestAction(requestId: String): DebugResponse? {
        //TODO
        return null
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage): DebugResponse? {
        val method: String
        val url: String

        val parsedResponse = messageContainer.converter(response)

        val req = messageContainer.storage.findRequest(parsedResponse)
        if (req == null) {
            method = parsedResponse.method ?: "<unknown>"
            url = parsedResponse.url ?: "<unknown>"
        } else {
            method = req.method ?: parsedResponse.method ?: "<unknown>"
            url = req.url ?: parsedResponse.url ?: "<unknown>"
        }

        val future = CompletableFuture<DebugResponse?>()

        @Suppress("UNCHECKED_CAST")
        waitingMessagesModel.addMessage(DebugMessageEntry(method,
                isRequest = false,
                url = url,
                response = parsedResponse,
                future = future as CompletableFuture<Any?>
        ))
        try {
            return future.get()
        } catch (e: Throwable) {
            logger.error(e)
            return null
        }
    }

    private fun checkRowSelectionState() {
        val index = waitingMessagesList.selectedRow
        if (index < 0) {
            cancelButton.isEnabled = false
            sendButton.isEnabled = false
            detailView.clearMessage()
            return
        }
        showDetailFor(waitingMessagesModel.getMessageAt(index))
    }

    private fun showDetailFor(debugMessageEntry: DebugMessageEntry) {
        cancelButton.isEnabled = true
        sendButton.isEnabled = true
        detailView.showDetails(debugMessageEntry)
    }

    private fun onCancelClicked() {
        val index = waitingMessagesList.selectedRow
        if (index < 0) {
            cancelButton.isEnabled = false
            sendButton.isEnabled = false
            detailView.clearMessage()
            return
        }
        val model = waitingMessagesModel.getMessageAt(index)
        waitingMessagesModel.removeMessage(model)
        if (waitingMessagesModel.rowCount == 0)
            waitingMessagesList.clearSelection()

        model.future.complete(null)
    }

    private fun onSubmitClicked() {
        val index = waitingMessagesList.selectedRow
        if (index < 0) {
            cancelButton.isEnabled = false
            sendButton.isEnabled = false
            detailView.clearMessage()
            return
        }
        val model = waitingMessagesModel.getMessageAt(index)
        waitingMessagesModel.removeMessage(model)
        if (waitingMessagesModel.rowCount == 0)
            waitingMessagesList.clearSelection()

        detailView.save(model)

        model.future.complete(buildDebuggerReply(model))
    }

    private fun buildDebuggerReply(waitingMessageEntry: DebugMessageEntry): Any? {
        return if (waitingMessageEntry.isRequest)
            null
        else
            buildResponse(waitingMessageEntry)
    }

    private fun buildResponse(waitingMessageEntry: DebugMessageEntry): DebugResponse? {
        val resp = waitingMessageEntry.response ?: return null
        return DebugResponse(resp.statusCode ?: 200,
                resp.statusLine ?: "OK",
                waitingMessageEntry.modifiedHeaders ?: resp.headers,
                resp.bodyAsNormalBase64,
                resp.bodyFormat.subtype)
    }
}

