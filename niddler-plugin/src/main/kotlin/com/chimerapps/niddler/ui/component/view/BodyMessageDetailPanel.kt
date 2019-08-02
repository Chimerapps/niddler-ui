package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.bodyRendererForFormat
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.icons.AllIcons
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JToggleButton
import javax.swing.JToolBar

class BodyMessageDetailPanel : JPanel(BorderLayout()) {

    private val structuredButton = JToggleButton("Structured", AllIcons.Hierarchy.Subtypes)
    private val prettyButton = JToggleButton("Pretty", IncludedIcons.Action.pretty)
    private val rawButton = JToggleButton("Raw", AllIcons.Debugger.Db_primitive)

    private var previousStructuredComponent: JComponent? = null
    private var previousPrettyComponent: JComponent? = null
    private var previousRawComponent: JComponent? = null
    private var currentAddedComponent: JComponent? = null

    private var currentMessageRenderer: BodyRenderer<ParsedNiddlerMessage>? = null
    private var currentMessage: ParsedNiddlerMessage? = null
    private var currentView = CurrentView.STRUCTURED

    init {
        JToolBar().also {
            it.isFloatable = false
            it.add(Box.createHorizontalGlue())
            it.add(structuredButton)
            it.add(prettyButton)
            it.add(rawButton)

            add(it, BorderLayout.NORTH)
        }

        ButtonGroup().also {
            it.add(structuredButton)
            it.add(prettyButton)
            it.add(rawButton)
        }
        structuredButton.addItemListener { switchToStructured() }
        prettyButton.addItemListener { switchToPretty() }
        rawButton.addItemListener { switchToRaw() }
    }

    fun init(message: ParsedNiddlerMessage) {
        val renderer = bodyRendererForFormat(message.bodyFormat)
        currentMessageRenderer = renderer
        currentMessage = message

        structuredButton.isVisible = renderer?.supportsStructure ?: false
        prettyButton.isVisible = renderer?.supportsPretty ?: false
        rawButton.isVisible = renderer?.supportsRaw ?: false

        currentAddedComponent?.let(::remove)
        currentAddedComponent = null
        if (renderer != null) {
            updateComponent()
        }

        revalidate()
        repaint()
    }

    private fun switchToStructured() {
        if (currentView == CurrentView.STRUCTURED)
            return
        currentView = CurrentView.STRUCTURED
        updateComponent()
    }

    private fun switchToPretty() {
        if (currentView == CurrentView.PRETTY)
            return
        currentView = CurrentView.PRETTY
        updateComponent()
    }

    private fun switchToRaw() {
        if (currentView == CurrentView.RAW)
            return
        currentView = CurrentView.RAW
        updateComponent()
    }

    private fun updateComponent() {
        val renderer = currentMessageRenderer ?: return
        val message = currentMessage ?: return

        currentAddedComponent?.let(::remove)
        currentAddedComponent = null

        val supportedItems = mutableListOf<CurrentView>()
        if (structuredButton.isVisible) {
            supportedItems += CurrentView.STRUCTURED
        }
        if (prettyButton.isVisible) {
            supportedItems += CurrentView.PRETTY
        }
        if (rawButton.isVisible) {
            supportedItems += CurrentView.RAW
        }
        if (currentView !in supportedItems) {
            if (supportedItems.isEmpty())
                return

            currentView = supportedItems.first()
        }
        structuredButton.isSelected = currentView == CurrentView.STRUCTURED
        prettyButton.isSelected = currentView == CurrentView.PRETTY
        rawButton.isSelected = currentView == CurrentView.RAW

        when (currentView) {
            CurrentView.STRUCTURED -> {
                previousStructuredComponent = renderer.structured(message, previousStructuredComponent).also {
                    currentAddedComponent = it
                    add(it, BorderLayout.CENTER)
                }
            }
            CurrentView.PRETTY -> {
                previousPrettyComponent = renderer.pretty(message, previousPrettyComponent).also {
                    currentAddedComponent = it
                    add(it, BorderLayout.CENTER)
                }
            }
            CurrentView.RAW -> {
                previousRawComponent = renderer.raw(message, previousRawComponent).also {
                    currentAddedComponent = it
                    add(it, BorderLayout.CENTER)
                }
            }
        }

        revalidate()
        repaint()
    }

    private enum class CurrentView {
        STRUCTURED, PRETTY, RAW
    }

}