package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.util.ui.dispatchMain
import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerMessageListener
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class MessageDetailView(project: Project,
                        disposable: Disposable,
                        private val storage: NiddlerMessageStorage<ParsedNiddlerMessage>) : JPanel(BorderLayout()), MessageSelectionListener, NiddlerMessageListener {

    var currentMessage: ParsedNiddlerMessage? = null
        set(value) {
            if (field === value)
                return
            field = value
            if (value == null) {
                setEmpty()
            } else {
                updateUi(value)
            }
        }

    private var currentlyEmpty = false
    private val generalDetailPanel = GeneralMessageDetailPanel(project)
    private val bodyDetailPanel = BodyMessageDetailPanel(project)
    private val tabsContainer: RunnerLayoutUi

    init {
        setEmpty()

        tabsContainer = RunnerLayoutUi.Factory.getInstance(project).create("niddler-ui", "Detail tabs", "Some session name?", disposable)
        val detailsContent = tabsContainer.createContent("DetailViewDetails", generalDetailPanel, "Details", null, null)
        detailsContent.setPreferredFocusedComponent { generalDetailPanel }
        detailsContent.isCloseable = false
        tabsContainer.addContent(detailsContent, -1, PlaceInGrid.center, false)

        val bodyContent = tabsContainer.createContent("DetailViewBody", bodyDetailPanel, "Body", null, null)
        bodyContent.setPreferredFocusedComponent { bodyDetailPanel }
        bodyContent.isCloseable = false
        tabsContainer.addContent(bodyContent, -1, PlaceInGrid.center, false)
    }

    override fun onMessageSelectionChanged(message: ParsedNiddlerMessage?) {
        currentMessage = message
    }

    private fun setEmpty() {
        if (currentlyEmpty)
            return

        currentlyEmpty = true
        removeAll()
        add(JLabel("Select a request/response", SwingConstants.CENTER), BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun setDetailUI() {
        if (!currentlyEmpty)
            return

        currentlyEmpty = false
        removeAll()
        add(tabsContainer.component, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun updateUi(message: ParsedNiddlerMessage) {
        setDetailUI()

        val other = if (message.isRequest)
            storage.findResponse(message)
        else
            storage.findRequest(message)

        generalDetailPanel.init(message, other)
        bodyDetailPanel.init(message)
    }

    override fun onServiceMessage(niddlerMessage: NiddlerMessage) {
        super.onServiceMessage(niddlerMessage)
        dispatchMain {
            val currentMessage = generalDetailPanel.currentMessage ?: return@dispatchMain
            if (generalDetailPanel.needsResponse && !niddlerMessage.isRequest && niddlerMessage.requestId == currentMessage.requestId) {
                updateUi(currentMessage)
            }
        }
    }
}

interface MessageSelectionListener {
    fun onMessageSelectionChanged(message: ParsedNiddlerMessage?)
}

interface BaseUrlHideListener {
    fun hideBaseUrl(baseUrl: String)
    fun showBaseUrl(baseUrl: String)
}