package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*
import javax.swing.text.Document

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
abstract class NiddlerStructuredDataPanel(hasPretty: Boolean, protected val message: ParsedNiddlerMessage) : JPanel() {

    private var currentContentPanel: JComponent? = null

    protected lateinit var structuredView: JComponent

    private val treeButton: JToggleButton
    private val prettyButton: JToggleButton
    private val rawButton: JToggleButton
    private var toolbar: JToolBar
    private val monospaceFont: Font

    init {
        layout = BorderLayout()

        treeButton = JToggleButton("Structure", ImageIcon(javaClass.getResource("/ic_as_tree.png")))
        prettyButton = JToggleButton("Pretty", ImageIcon(javaClass.getResource("/ic_pretty.png")))
        rawButton = JToggleButton("Raw", ImageIcon(javaClass.getResource("/ic_raw.png")))

        val buttonGroup = ButtonGroup()
        buttonGroup.add(treeButton)
        if (hasPretty)
            buttonGroup.add(prettyButton)
        buttonGroup.add(rawButton)

        toolbar = JToolBar()
        toolbar.isFloatable = false
        toolbar.add(Box.createGlue())
        toolbar.add(treeButton)
        if (hasPretty)
            toolbar.add(prettyButton)
        toolbar.add(rawButton)

        treeButton.isSelected = true
        treeButton.addItemListener { if (treeButton.isSelected) initAsTree() }
        if (hasPretty)
            prettyButton.addItemListener { if (prettyButton.isSelected) initAsPretty() }
        rawButton.addItemListener { if (rawButton.isSelected) initAsRaw() }

        monospaceFont = Font("Monospaced", Font.PLAIN, 10)
    }

    protected fun initUI() {
        add(toolbar, BorderLayout.NORTH)

        createStructuredView()
        initAsTree()
    }

    protected abstract fun createStructuredView()

    protected open fun createPrettyPrintedView(doc: Document) {}

    private fun initAsTree() {
        replacePanel(JScrollPane(structuredView))
    }

    private fun initAsPretty() {
        val textArea = JTextArea()
        textArea.isEditable = false
        textArea.font = monospaceFont
        createPrettyPrintedView(textArea.document)
        replacePanel(JScrollPane(textArea))
    }

    private fun initAsRaw() {
        val textArea = JTextArea()
        textArea.text = message.message.getBodyAsString(message.bodyFormat.encoding)
        textArea.isEditable = false
        textArea.font = monospaceFont
        replacePanel(JScrollPane(textArea))
    }

    private fun replacePanel(newContents: JComponent) {
        if (currentContentPanel != null) remove(currentContentPanel)
        add(newContents, BorderLayout.CENTER)
        currentContentPanel = newContents
        revalidate()
        repaint()
    }

}