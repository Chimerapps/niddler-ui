package com.chimerapps.niddler.ui.provider

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.QuickConnectionInfo
import com.chimerapps.niddler.ui.settings.NiddlerProjectSettings
import com.chimerapps.niddler.ui.util.execution.NiddlerAutomaticConnectionHelper
import com.chimerapps.niddler.ui.util.execution.ProcessExecutionListener
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

    companion object {
        const val START_REGEX = ".*Niddler Server running on (\\d+)\\s+\\[([a-zA-Z0-9]+)\\](\\[waitingForDebugger=(true|false)\\])?\\s*"
        private const val START_PROCESS_REGEX = "\\S+ \\S+ (\\d+)-(\\d+)/.*"
    }

    private val matcher = Pattern.compile(START_REGEX).matcher("")
    private val startProcessMatcher = Pattern.compile(START_PROCESS_REGEX).matcher("")

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        if (!matcher.reset(line).matches()) return null
        val port = matcher.group(1).toInt()
        val tag = matcher.group(2)
        val tagGroupStart = matcher.start(2)
        val tagGroupEnd = matcher.end(2)
        val waitingForDebugger = if (matcher.groupCount() >= 4) {
            (matcher.group(4) == "true")
        } else false

        val processId = if (startProcessMatcher.reset(line.trim()).matches())
            startProcessMatcher.group(1).toIntOrNull()
        else
            null

        val settings = NiddlerProjectSettings.instance(project)
        if (processId?.let(ProcessExecutionListener.autoConnectHelper::remove) == true) {
            if (settings.automaticallyReconnect == true) {
                val info = QuickConnectionInfo(port, tag, null)
                val reuseSession = settings.reuseSession == true
                val connectUsingDebugger = settings.connectUsingDebugger == true || waitingForDebugger
                NiddlerAutomaticConnectionHelper.connect(
                        project = project,
                        info = info,
                        reuseSession = reuseSession,
                        connectUsingDebugger = connectUsingDebugger
                )
            }
        }

        val textStartOffset = entireLength - line.length
        return Filter.Result(textStartOffset + tagGroupStart,
                textStartOffset + tagGroupEnd, NiddlerConnectHyperlinkInfo(port, tag, settings.connectUsingDebugger == true || waitingForDebugger))
    }

}

class NiddlerConnectHyperlinkInfo(private val port: Int, private val tag: String, private val withDebugger: Boolean) : HyperlinkInfo {

    override fun navigate(project: Project) {
        val (niddlerWindow, toolWindow) = NiddlerToolWindow.get(project) ?: return

        if (!toolWindow.isVisible) {
            toolWindow.show {
                niddlerWindow.newSessionForTag(tag, withDebugger)
            }
        } else {
            niddlerWindow.newSessionForTag(tag, withDebugger)
        }
    }

}