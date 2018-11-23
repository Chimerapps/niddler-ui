package com.icapps.niddler.ui.component

import com.icapps.niddler.ui.form.components.StackTraceComponent
import com.intellij.execution.filters.ExceptionFilter
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.impl.EditorHyperlinkSupport
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import java.awt.Component

class IntelliJStackTraceComponent(project: Project) : StackTraceComponent {

    private val editor: EditorEx = ConsoleViewUtil.setupConsoleEditor(project, false, false)
    private val document = editor.document
    private val exceptionFilter = ExceptionFilter(GlobalSearchScope.allScope(project))
    private val hyperLinks = EditorHyperlinkSupport(editor, project)

    override val asComponent: Component = editor.component

    override fun setStackTrace(stackTrace: List<String>?) {
        document.deleteString(0, document.textLength)

        stackTrace?.forEach { element ->
            val info = exceptionFilter.applyFilter(element, element.length)?.firstHyperlinkInfo
            document.insertString(document.textLength, "\n" + element)
            if (info != null) hyperLinks.createHyperlink(document.textLength - element.length, document.textLength, null, info)
        }
    }

    override fun invalidate() {
        asComponent.invalidate()
    }

    override fun repaint() {
        asComponent.repaint()
    }

}