package com.chimerapps.niddler.ui.component

import com.chimerapps.niddler.ui.actions.ConnectAction
import com.chimerapps.niddler.ui.actions.DisconnectAction
import com.chimerapps.niddler.ui.actions.LinkedAction
import com.chimerapps.niddler.ui.actions.TimelineAction
import com.chimerapps.niddler.ui.component.view.MessagesView
import com.chimerapps.niddler.ui.component.view.TimelineView
import com.icapps.niddler.lib.model.InMemoryNiddlerMessageStorage
import com.icapps.niddler.lib.model.NiddlerMessageBodyParser
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.classifier.HeaderBodyClassifier
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel

class NiddlerSessionWindow : JPanel(BorderLayout()) {

    private val rootContent = JPanel(BorderLayout())
    private val connectToolbar = setupConnectToolbar()
    private val viewToolbar = setupViewToolbar()

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
    private var currentMessagesView: MessagesView? = null
    private val bodyParser = NiddlerMessageBodyParser(HeaderBodyClassifier(emptyList())) //TODO extensions!
    private val messageContainer = NiddlerMessageContainer(bodyParser::parseBody, InMemoryNiddlerMessageStorage())

    init {
        add(rootContent, BorderLayout.CENTER)
        updateView()
    }

    fun currentViewModeUnselected() {
        when (currentViewMode) {
            ViewMode.VIEW_MODE_TIMELINE -> currentViewMode = ViewMode.VIEW_MODE_LINKED
            ViewMode.VIEW_MODE_LINKED -> currentViewMode = ViewMode.VIEW_MODE_TIMELINE
        }
    }

    fun onClosed() {
        //TODO disconnect AND clear
    }

    private fun setupConnectToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        actionGroup.add(ConnectAction(this) {
            connectionMode = ConnectionMode.MODE_CONNECTED
        })
        actionGroup.add(DisconnectAction(this) {
            connectionMode = ConnectionMode.MODE_DISCONNECTED
        })

        val toolbar = ActionManager.getInstance().createActionToolbar("Niddler", actionGroup, true)
        rootContent.add(toolbar.component, BorderLayout.NORTH)
        return toolbar
    }

    private fun setupViewToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        val timelineAction = TimelineAction(this)
        actionGroup.add(timelineAction)

        val linkedAction = LinkedAction(this)
        actionGroup.add(linkedAction)

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
        (currentMessagesView as? Component)?.let(rootContent::remove)
        currentMessagesView = messagesView
        rootContent.add(messagesView, BorderLayout.CENTER)
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