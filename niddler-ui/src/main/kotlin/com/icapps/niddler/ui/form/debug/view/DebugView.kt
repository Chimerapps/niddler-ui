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
import java.awt.BorderLayout
import java.util.concurrent.CompletableFuture
import javax.swing.JPanel
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

    private val waitingMessagesModel = MessagesModel()
    private val waitingMessagesList = JTable(waitingMessagesModel)
    private val detailView = DebugDetailView(componentsFactory)

    private val splitter = componentsFactory.createSplitPane()

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

        val scroller = componentsFactory.createScrollPane()
        scroller.setViewportView(waitingMessagesList)

        add(splitter.asComponent, BorderLayout.CENTER)
        splitter.left = scroller
        splitter.right = detailView
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

        waitingMessagesModel.addMessage(DebugMessageEntry(method,
                isRequest = false,
                url = url,
                response = parsedResponse,
                future = future
        ))
        messagesUpdateListener(waitingMessagesList.rowCount)
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
            //TODO clear right
            return
        }
        showDetailFor(waitingMessagesModel.getMessageAt(index))
    }

    private fun showDetailFor(debugMessageEntry: DebugMessageEntry) {
        //TODO
    }
}

