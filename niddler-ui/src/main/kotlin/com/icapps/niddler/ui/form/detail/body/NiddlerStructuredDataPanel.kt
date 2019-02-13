package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.form.NiddlerStructuredViewPopupMenu
import com.icapps.niddler.ui.util.ClipboardUtil
import com.icapps.niddler.ui.util.loadIcon
import java.awt.BorderLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JToggleButton
import javax.swing.JToolBar
import javax.swing.text.Document

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
abstract class NiddlerStructuredDataPanel(hasTree: Boolean, hasPretty: Boolean, savedState: Map<String, Any>?, protected val message: ParsedNiddlerMessage) : JPanel() {

    private companion object {
        private const val STATE_STRUCTURE = "current_state"
    }

    private var currentContentPanel: JComponent? = null

    protected lateinit var structuredView: JComponent

    private val treeButton: JToggleButton
    private val prettyButton: JToggleButton
    private val rawButton: JToggleButton
    private var toolbar: JToolBar
    private val monospaceFont: Font
    private var currentStructureState: NiddlerStructureState

    protected val popup = NiddlerStructuredViewPopupMenu(object : NiddlerStructuredViewPopupMenu.Listener {
        override fun onCopyKeyClicked(key: Any) {
            ClipboardUtil.copyToClipboard(StringSelection(key.toString()))
        }

        override fun onCopyValueClicked(value: Any) {
            ClipboardUtil.copyToClipboard(StringSelection(value.toString()))
        }

    })

    init {
        layout = BorderLayout()

        treeButton = JToggleButton("Structure", loadIcon("/ic_as_tree.png"))
        prettyButton = JToggleButton("Pretty", loadIcon("/ic_pretty.png"))
        rawButton = JToggleButton("Raw", loadIcon("/ic_raw.png"))

        val buttonGroup = ButtonGroup()
        if (hasTree)
            buttonGroup.add(treeButton)
        if (hasPretty)
            buttonGroup.add(prettyButton)
        buttonGroup.add(rawButton)

        currentStructureState = determineDefaultStructureState(hasTree, hasPretty, savedState?.get(STATE_STRUCTURE) as? NiddlerStructureState)

        toolbar = JToolBar()
        toolbar.isFloatable = false
        toolbar.add(Box.createGlue())
        if (hasTree)
            toolbar.add(treeButton)
        if (hasPretty)
            toolbar.add(prettyButton)
        toolbar.add(rawButton)

        treeButton.isSelected = currentStructureState == NiddlerStructureState.STATE_TREE
        prettyButton.isSelected = currentStructureState == NiddlerStructureState.STATE_PRETTY
        rawButton.isSelected = currentStructureState == NiddlerStructureState.STATE_RAW

        treeButton.addItemListener {
            if (treeButton.isSelected) {
                currentStructureState = NiddlerStructureState.STATE_TREE
                initAsTree()
            }
        }
        prettyButton.addItemListener {
            if (prettyButton.isSelected) {
                currentStructureState = NiddlerStructureState.STATE_PRETTY
                initAsPretty()
            }
        }
        rawButton.addItemListener {
            if (rawButton.isSelected) {
                currentStructureState = NiddlerStructureState.STATE_RAW
                initAsRaw()
            }
        }
        if (treeButton.isSelected) treeButton.requestFocusInWindow()
        if (prettyButton.isSelected) treeButton.requestFocusInWindow()
        if (rawButton.isSelected) treeButton.requestFocusInWindow()

        monospaceFont = Font("Monospaced", Font.PLAIN, 10)
    }

    protected fun initUI() {
        add(toolbar, BorderLayout.NORTH)

        createStructuredView()
        when {
            treeButton.isSelected -> initAsTree()
            prettyButton.isSelected -> initAsPretty()
            else -> initAsRaw()
        }
    }

    open fun saveState(data: MutableMap<String, Any>) {
        data[STATE_STRUCTURE] = currentStructureState
    }

    protected open fun createStructuredView() {}

    protected open fun createPrettyPrintedView(doc: Document) {}

    private fun initAsTree() {
        replacePanel(JScrollPane(structuredView))
    }

    protected open fun initAsPretty() {
        val textArea = JTextArea()
        textArea.isEditable = false
        textArea.font = monospaceFont
        createPrettyPrintedView(textArea.document)
        replacePanel(JScrollPane(textArea))
    }

    protected open fun initAsRaw() {
        val textArea = JTextArea()
        textArea.text = message.message.getBodyAsString(message.bodyFormat.encoding)
        textArea.isEditable = false
        textArea.font = monospaceFont
        replacePanel(JScrollPane(textArea))
    }

    protected fun replacePanel(newContents: JComponent) {
        if (currentContentPanel != null) remove(currentContentPanel)
        add(newContents, BorderLayout.CENTER)
        currentContentPanel = newContents
        revalidate()
        repaint()
    }

    private fun determineDefaultStructureState(hasTree: Boolean, hasPretty: Boolean, oldState: NiddlerStructureState?): NiddlerStructureState {
        if (oldState != null) {
            when (oldState) {
                NiddlerStructureState.STATE_PRETTY -> if (hasPretty) return oldState
                NiddlerStructureState.STATE_TREE -> if (hasTree) return oldState
                NiddlerStructureState.STATE_RAW -> return oldState
            }
        }

        return when {
            hasTree -> NiddlerStructureState.STATE_TREE
            hasPretty -> NiddlerStructureState.STATE_PRETTY
            else -> NiddlerStructureState.STATE_RAW
        }
    }

}

enum class NiddlerStructureState {
    STATE_PRETTY,
    STATE_TREE,
    STATE_RAW
}