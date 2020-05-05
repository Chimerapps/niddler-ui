package com.chimerapps.niddler.ui.component.view

import com.intellij.execution.filters.ExceptionFilter
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.HyperlinkInfoBase
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBFont
import java.awt.Color
import java.awt.Cursor
import java.awt.Font
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.lang.reflect.Constructor
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.UIManager
import javax.swing.text.AttributeSet
import javax.swing.text.Document
import javax.swing.text.Style
import javax.swing.text.StyleConstants

class StackTraceView(project: Project) : JPanel() {

    companion object {
        val isSupported: Boolean = try {
            Class.forName("com.intellij.execution.filters.ExceptionFilter")
            true
        } catch (e: Throwable) {
            false
        }

        val dartConsoleFilterConstructor: Constructor<*>? by lazy {
            try {
                Class.forName("com.jetbrains.lang.dart.ide.runner.DartConsoleFilter").declaredConstructors.find {
                    it.parameterCount == 1 && it.parameterTypes[0].isAssignableFrom(Project::class.java)
                }
            } catch (e: Throwable) {
                null
            }
        }

        fun isDartSupported(project: Project): Boolean = try {
            dartConsoleFilterConstructor?.newInstance(project) is Filter
        } catch (e: Throwable) {
            false
        }
    }

    private val normalFont = JBFont.create(Font("Monospaced", 0, 12))
    private val textArea: JTextPane = JTextPane()
    private val exceptionFilter = if (isSupported) ExceptionHelper(project, textArea) else null
    private val dartExceptionFilter = if (isDartSupported(project)) DartExceptionHelper(project, textArea) else null

    init {
        textArea.isEditable = false
        textArea.font = normalFont
        textArea.border = null
        textArea.foreground = UIManager.getColor("Label.foreground")
        textArea.background = UIManager.getColor("EditorPane.background")

        if (exceptionFilter != null || dartExceptionFilter != null) {
            val defaultCursor = textArea.cursor

            textArea.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(mouseEvent: MouseEvent) {
                    if (mouseEvent.button == MouseEvent.BUTTON1 && !mouseEvent.isPopupTrigger) {
                        (exceptionFilter?.getHyperlinkInfoByPoint(mouseEvent.point) ?: dartExceptionFilter?.getHyperlinkInfoByPoint(mouseEvent.point))?.execute()
                    }
                }
            })
            textArea.addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    val info = exceptionFilter?.getHyperlinkInfoByPoint(e.point) ?: dartExceptionFilter?.getHyperlinkInfoByPoint(e.point)
                    if (info != null) {
                        textArea.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    } else {
                        textArea.cursor = defaultCursor
                    }
                }
            })
        }

        val style = textArea.addStyle("Style", null)
        StyleConstants.setForeground(style, UIManager.getColor("link.foreground") ?: Color(0x56, 0x98, 0xED, 0xFF))
        StyleConstants.setUnderline(style, true)
        exceptionFilter?.linkStyle = style
        dartExceptionFilter?.linkStyle = style

        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(textArea)
    }

    fun setStackTrace(stackTrace: Collection<String>?) {
        val document = textArea.styledDocument
        document.remove(0, document.length)
        if (stackTrace == null || stackTrace.isEmpty()) {
            textArea.isVisible = false
            return
        }
        textArea.isVisible = true
        when {
            exceptionFilter?.anyMatch(stackTrace) == true -> {
                exceptionFilter.update(stackTrace, document)
            }
            dartExceptionFilter?.anyMatch(stackTrace) == true -> {
                dartExceptionFilter.update(stackTrace, document)
            }
            else -> {
                stackTrace.forEachIndexed { index, element ->
                    if (index > 0) {
                        document.insertString(document.length, "\n", null)
                    }
                    document.insertString(document.length, element, null)
                }
            }
        }
    }
}

private class ExceptionHelper(project: Project, textArea: JTextPane) : BaseExceptionHelper(project, ExceptionFilter(GlobalSearchScope.allScope(project)), textArea)

private class DartExceptionHelper(project: Project,
                                  textArea: JTextPane)
    : BaseExceptionHelper(project,
        StackTraceView.dartConsoleFilterConstructor!!.newInstance(project) as Filter,
        textArea)

internal open class BaseExceptionHelper(private val project: Project, private val filter: Filter, private val textArea: JTextPane) {
    private val lines = mutableListOf<Pair<HyperlinkInfo?, IntRange>>()

    lateinit var linkStyle: Style

    fun getHyperlinkInfoByPoint(point: Point): HyperlinkContainer? {
        val pos = textArea.viewToModel(point)
        val info = lines.find { pos in it.second }?.first ?: return null

        return HyperlinkContainer(textArea, project, point, info)
    }

    fun anyMatch(stackTrace: Collection<String>): Boolean {
        return stackTrace.any { matchesHelper(it) }
    }

    fun matchesHelper(element: String): Boolean {
        return filter.applyFilter(element, element.length)?.firstHyperlinkInfo != null
    }

    fun update(stackTrace: Collection<String>, document: Document) {
        lines.clear()
        stackTrace.forEachIndexed { index, element ->
            val info = filter.applyFilter(element, element.length)?.firstHyperlinkInfo

            lines += info to document.length..document.length + element.length

            if (index > 0) {
                document.insertString(document.length, "\n", null)
            }
            val style: AttributeSet? = if (info != null) linkStyle else null

            document.insertString(document.length, element, style)
        }
    }
}

internal class HyperlinkContainer(private val textArea: JTextPane, private val project: Project, private val point: Point, private val hyperlinkInfo: HyperlinkInfo) {
    fun execute() {
        if (hyperlinkInfo is HyperlinkInfoBase) {
            val event = MouseEvent(textArea, 0, 0, 0, point.x, point.y, 1, false)
            hyperlinkInfo.navigate(project, RelativePoint(event))
        } else {
            hyperlinkInfo.navigate(project)
        }
    }
}