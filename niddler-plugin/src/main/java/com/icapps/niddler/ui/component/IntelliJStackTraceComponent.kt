package com.icapps.niddler.ui.component

import com.icapps.niddler.ui.form.components.StackTraceComponent
import com.intellij.execution.filters.ExceptionFilter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.HyperlinkInfoBase
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.awt.RelativePoint
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Font
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.UIManager
import javax.swing.text.AttributeSet
import javax.swing.text.Style
import javax.swing.text.StyleConstants


class IntelliJStackTraceComponent(private val project: Project) : StackTraceComponent {

    private val panelContainer = JPanel().also { it.layout = BoxLayout(it, BoxLayout.Y_AXIS) }
    private val exceptionFilter = ExceptionFilter(GlobalSearchScope.allScope(project))
    private val normalFont = Font("Monospaced", 0, 12)
    private val textArea: JTextPane = JTextPane()
    private val style: Style
    private val lines = mutableListOf<Pair<HyperlinkInfo?, IntRange>>()

    override val asComponent: Component = panelContainer

    init {
        panelContainer.add(textArea)
        textArea.font = normalFont
        textArea.isEditable = false
        textArea.border = null
        textArea.foreground = UIManager.getColor("Label.foreground")
        textArea.background = null

        style = textArea.addStyle("Style", null)
        StyleConstants.setForeground(style, Color.blue)
        StyleConstants.setUnderline(style, true)

        val defaultCursor = textArea.cursor

        textArea.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(mouseEvent: MouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1 && !mouseEvent.isPopupTrigger) {
                    getHyperlinkInfoByPoint(mouseEvent.point)?.let { makeRunnable(it, mouseEvent) }?.run()
                }
            }
        })
        textArea.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val info = getHyperlinkInfoByPoint(e.point)
                if (info != null) {
                    textArea.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                } else {
                    textArea.cursor = defaultCursor
                }
            }
        })
    }

    private fun getHyperlinkInfoByPoint(point: Point): HyperlinkInfo? {
        val pos = textArea.viewToModel(point)
        return lines.find { pos in it.second }?.first
    }

    private fun makeRunnable(hyperlinkInfo: HyperlinkInfo, mouseEvent: MouseEvent): Runnable {
        return Runnable {
            if (hyperlinkInfo is HyperlinkInfoBase) {
                val point = mouseEvent.point
                val event = MouseEvent(textArea, 0, 0, 0, point.x, point.y, 1, false)
                hyperlinkInfo.navigate(project, RelativePoint(event))
            } else {
                hyperlinkInfo.navigate(project)
            }
        }
    }

    override fun setStackTrace(stackTrace: List<String>?) {
        val document = textArea.styledDocument
        textArea.text
        document.remove(0, document.length)
        lines.clear()
        if (stackTrace == null) {
            return
        }

        stackTrace.forEachIndexed { index, element ->
            val info = exceptionFilter.applyFilter(element, element.length)?.firstHyperlinkInfo

            lines += info to document.length..document.length + element.length

            if (index > 0) {
                document.insertString(document.length, "\n", null)
            }
            val style: AttributeSet? = if (info != null) style else null

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