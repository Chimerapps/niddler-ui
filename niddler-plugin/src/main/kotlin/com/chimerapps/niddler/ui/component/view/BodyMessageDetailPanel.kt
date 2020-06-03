package com.chimerapps.niddler.ui.component.view

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.bodyRendererForFormat
import com.chimerapps.niddler.ui.model.renderer.impl.binary.BinaryBodyRenderer
import com.chimerapps.niddler.ui.util.ui.IncludedIcons
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
import com.chimerapps.niddler.ui.util.ui.chooseSaveFile
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.icapps.niddler.lib.model.ObservingToken
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.model.ParsedNiddlerMessageProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLoadingPanel
import java.awt.BorderLayout
import java.io.FileOutputStream
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JToggleButton
import javax.swing.JToolBar

class BodyMessageDetailPanel(private val project: Project,
                             private val parsedNiddlerMessageProvider: ParsedNiddlerMessageProvider) : JBLoadingPanel(BorderLayout(), project) {

    private val structuredButton = JToggleButton("Structured", AllIcons.Hierarchy.Subtypes)
    private val prettyButton = JToggleButton("Pretty", IncludedIcons.Action.pretty)
    private val rawButton = JToggleButton("Raw", AllIcons.Debugger.Db_primitive)
    private val saveButton = JButton("", AllIcons.Actions.Menu_saveall).also {
        it.toolTipText = "Save body"
    }

    private var previousStructuredComponent: JComponent? = null
    private var previousPrettyComponent: JComponent? = null
    private var previousRawComponent: JComponent? = null
    private var currentAddedComponent: JComponent? = null

    private var currentMessageRenderer: BodyRenderer<ParsedNiddlerMessage>? = null
    private var currentMessage: ParsedNiddlerMessage? = null
    private var currentView = CurrentView.STRUCTURED
    private var observingToken: ObservingToken? = null

    init {
        JToolBar().also {
            it.isFloatable = false
            it.add(saveButton)
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
        structuredButton.addItemListener { switchToStructured(requestFocus = true) }
        prettyButton.addItemListener { switchToPretty(requestFocus = true) }
        rawButton.addItemListener { switchToRaw(requestFocus = true) }
        saveButton.addActionListener { saveBody() }
    }

    fun init(message: NiddlerMessageInfo) {
        observingToken?.stopObserving()

        currentAddedComponent?.let { contentPanel.remove(it) }
        currentAddedComponent = null
        startLoading()
        observingToken = parsedNiddlerMessageProvider.provideParsedMessage(message).observe {
            initParsed(it)
            stopLoading()
            contentPanel.revalidate()
            contentPanel.repaint()
        }
    }

    private fun initParsed(message: ParsedNiddlerMessage) {
        val renderer = bodyRendererForFormat(message.bodyFormat) ?: BinaryBodyRenderer
        currentMessageRenderer = renderer
        currentMessage = message

        structuredButton.isVisible = renderer?.supportsStructure ?: false
        prettyButton.isVisible = renderer?.supportsPretty ?: false
        rawButton.isVisible = renderer?.supportsRaw ?: false

        saveButton.isVisible = (structuredButton.isVisible || prettyButton.isVisible || rawButton.isVisible) && (message.message.body != null)

        if (renderer != null) {
            updateComponent(claimFocus = false)
        }
    }

    private fun switchToStructured(requestFocus: Boolean) {
        if (currentView == CurrentView.STRUCTURED)
            return
        currentView = CurrentView.STRUCTURED
        updateComponent(requestFocus)
    }

    private fun switchToPretty(requestFocus: Boolean) {
        if (currentView == CurrentView.PRETTY)
            return
        currentView = CurrentView.PRETTY
        updateComponent(requestFocus)
    }

    private fun switchToRaw(requestFocus: Boolean) {
        if (currentView == CurrentView.RAW)
            return
        currentView = CurrentView.RAW
        updateComponent(requestFocus)
    }

    private fun updateComponent(claimFocus: Boolean) {
        val renderer = currentMessageRenderer ?: return
        val message = currentMessage ?: return

        currentAddedComponent?.let { contentPanel.remove(it) }
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
                previousStructuredComponent = renderer.structured(message, previousStructuredComponent, project, claimFocus).also {
                    currentAddedComponent = it
                    contentPanel.add(it, BorderLayout.CENTER)
                }
            }
            CurrentView.PRETTY -> {
                previousPrettyComponent = renderer.pretty(message, previousPrettyComponent, project, claimFocus).also {
                    currentAddedComponent = it
                    contentPanel.add(it, BorderLayout.CENTER)
                }
            }
            CurrentView.RAW -> {
                previousRawComponent = renderer.raw(message, previousRawComponent, project, claimFocus).also {
                    currentAddedComponent = it
                    contentPanel.add(it, BorderLayout.CENTER)
                }
            }
        }

        contentPanel.revalidate()
        contentPanel.repaint()
        if (claimFocus) {
            currentAddedComponent?.let { component ->
                if (component is JScrollPane && component.componentCount > 0) {
                    component.getComponent(0).requestFocus()
                } else {
                    component.requestFocus()
                }
            }
        }
    }

    private fun saveBody() {
        val message = currentMessage ?: return
        message.message.getBodyAsBytes?.let { bytes ->
            val chosenFile = chooseSaveFile("Save to", "") ?: return
            runWriteAction {
                FileOutputStream(chosenFile).use { it.write(bytes) }

                NotificationUtil.info("Save complete", "<html>Export completed to <a href=\"file://${chosenFile.absolutePath}\">${chosenFile.name}</a></html>", project)
            }
        }
    }

    private enum class CurrentView {
        STRUCTURED, PRETTY, RAW
    }

}