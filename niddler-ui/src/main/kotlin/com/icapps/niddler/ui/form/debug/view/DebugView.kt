package com.icapps.niddler.ui.form.debug.view

import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.setColumnFixedWidth
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.ListSelectionModel

/**
 * @author nicolaverbeeck
 */
class DebugView(private val componentsFactory: ComponentsFactory) : JPanel(BorderLayout()) {

    private val waitingMessagesModel = MessagesModel()
    private val waitingMessagesList = JTable(waitingMessagesModel)

    private val splitter = componentsFactory.createSplitPane()

    init {
        waitingMessagesList.apply {
            fillsViewportHeight = false
            rowHeight = 32
            showHorizontalLines = true
            showVerticalLines = false

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setColumnFixedWidth(0, 90)
            tableHeader = null
        }

        val scroller = componentsFactory.createScrollPane()
        scroller.setViewportView(waitingMessagesList)

        add(splitter.asComponent, BorderLayout.CENTER)
        splitter.left = scroller
        splitter.resizePriority = 1.0
        splitter.asComponent.revalidate()

        waitingMessagesModel.addMessage(DebugMessageEntry("GET", true, "https://www.api.com/tokens/get"))
        waitingMessagesModel.addMessage(DebugMessageEntry("POST", false, "https://www.api.com/tokens/put"))
        waitingMessagesModel.addMessage(DebugMessageEntry("DELETE", true, "https://www.api.com/tokens/exec"))
    }

}

