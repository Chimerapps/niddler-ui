package com.chimerapps.niddler.ui.component.view

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import kotlin.random.Random

class MessageDetailView : JPanel(BorderLayout()), MessageSelectionListener {

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
    private val generalPanel = JPanel()
    private val headersPanel = JPanel()
    private val tracePanel = JPanel()
    private val contextPanel = JPanel()
    private val detailContainer = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        it.add(generalPanel)
        it.add(headersPanel)
        it.add(tracePanel)
        it.add(contextPanel)
    }

    private val contentScroller = JBScrollPane(JPanel(BorderLayout()).also { it.add(detailContainer, BorderLayout.NORTH) })

    init {
        setEmpty()

        generalPanel.border = BorderFactory.createTitledBorder(EmptyBorder(0, 0, 0, 0),
                "General", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        headersPanel.border = BorderFactory.createTitledBorder(EmptyBorder(0, 0, 0, 0),
                "Headers", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        tracePanel.border = BorderFactory.createTitledBorder(EmptyBorder(0, 0, 0, 0),
                "Stacktrace", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
        contextPanel.border = BorderFactory.createTitledBorder(EmptyBorder(0, 0, 0, 0),
                "Context", TitledBorder.ABOVE_TOP, TitledBorder.LEFT)
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
        add(contentScroller, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun updateUi(message: ParsedNiddlerMessage) {
        setDetailUI()

    }


}

interface MessageSelectionListener {
    fun onMessageSelectionChanged(message: ParsedNiddlerMessage?)
}