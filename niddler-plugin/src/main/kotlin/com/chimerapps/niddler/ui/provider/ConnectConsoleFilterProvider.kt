package com.chimerapps.niddler.ui.provider

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.intellij.execution.filters.ConsoleFilterProviderEx
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.search.GlobalSearchScope
import java.util.regex.Pattern

class ConnectConsoleFilterProvider : ConsoleFilterProviderEx {
    override fun getDefaultFilters(project: Project, scope: GlobalSearchScope): Array<Filter> {
        return arrayOf(NiddlerConnectFilter(project))
    }

    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(NiddlerConnectFilter(project))
    }

}

class NiddlerConnectFilter(private val project: Project) : Filter, DumbAware {

    private val matcher = Pattern.compile(".*Niddler Server running on (\\d+)\\s+\\[(\\S+)\\]\\s*").matcher("")

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        if (!matcher.reset(line).matches()) return null
        val port = matcher.group(1).toInt()
        val tag = matcher.group(2)
        val tagGroupStart = matcher.start(2)
        val tagGroupEnd = matcher.end(2)

        val textStartOffset = entireLength - line.length
        return Filter.Result(textStartOffset + tagGroupStart, textStartOffset + tagGroupEnd, NiddlerConnectHyperlinkInfo(port, tag))
    }

}

class NiddlerConnectHyperlinkInfo(private val port: Int, private val tag: String) : HyperlinkInfo {

    override fun navigate(project: Project) {
        val window = ToolWindowManager.getInstance(project).getToolWindow("Niddler") ?: return
        val niddlerWindow = window.contentManager.getContent(0)?.component as? NiddlerToolWindow ?: return

        if (!window.isVisible) {
            window.show {
                niddlerWindow.newSessionForTag(tag)
            }
        } else {
            niddlerWindow.newSessionForTag(tag)
        }
    }

}