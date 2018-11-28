package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.ui.form.components.StackTraceComponent
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.UIManager
import javax.swing.text.Style
import javax.swing.text.StyleConstants

class SwingStackTraceComponent : StackTraceComponent {

    private val panelContainer = JPanel().also { it.layout = BoxLayout(it, BoxLayout.Y_AXIS) }
    private val normalFont = Font("Monospaced", 0, 12)
    private val textArea: JTextPane = JTextPane()
    private val document = textArea.styledDocument
    private val style: Style

    override val asComponent: Component = panelContainer

    init {
        textArea.font = normalFont
        textArea.isEditable = false
        textArea.border = null
        textArea.foreground = UIManager.getColor("Label.foreground")
        textArea.background = null

        style = textArea.addStyle("Style", null)
        StyleConstants.setForeground(style, Color.blue)
        StyleConstants.setUnderline(style, true)
    }

    override fun setStackTrace(stackTrace: List<String>?) {
        document.remove(0, document.length)
        if (stackTrace == null) {
            return
        }

        stackTrace.forEachIndexed { index, element ->
            if (index > 0) {
                document.insertString(document.length, "\n", null)
            }
            document.insertString(document.length, element, style)
        }
    }

    override fun invalidate() {
        asComponent.invalidate()
    }

    override fun repaint() {
        asComponent.repaint()
    }

}