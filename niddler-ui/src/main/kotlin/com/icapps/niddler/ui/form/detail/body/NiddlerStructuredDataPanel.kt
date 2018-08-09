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
abstract class NiddlerStructuredDataPanel(hasTree: Boolean, hasPretty: Boolean, protected val message: ParsedNiddlerMessage) : JPanel() {

    private var currentContentPanel: JComponent? = null

    protected lateinit var structuredView: JComponent

    private val treeButton: JToggleButton
    private val prettyButton: JToggleButton
    private val rawButton: JToggleButton
    private var toolbar: JToolBar
    private val monospaceFont: Font

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

        toolbar = JToolBar()
        toolbar.isFloatable = false
        toolbar.add(Box.createGlue())
        if (hasTree)
            toolbar.add(treeButton)
        if (hasPretty)
            toolbar.add(prettyButton)
        toolbar.add(rawButton)

        if (hasTree) {
            treeButton.isSelected = true
            treeButton.addItemListener { if (treeButton.isSelected) initAsTree() }
        } else if (hasPretty) {
            prettyButton.isSelected = true
        } else {
            rawButton.isSelected = true
        }
        if (hasPretty)
            prettyButton.addItemListener { if (prettyButton.isSelected) initAsPretty() }
        rawButton.addItemListener { if (rawButton.isSelected) initAsRaw() }

        monospaceFont = Font("Monospaced", Font.PLAIN, 10)
    }

    protected fun initUI() {
        add(toolbar, BorderLayout.NORTH)

        createStructuredView()
        if (treeButton.isSelected)
            initAsTree()
        else if (prettyButton.isSelected)
            initAsPretty()
        else
            initAsRaw()
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

}