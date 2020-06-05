package com.chimerapps.niddler.ui.util.execution

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.QuickConnectionInfo
import com.chimerapps.niddler.ui.provider.NiddlerConnectFilter
import com.chimerapps.niddler.ui.settings.NiddlerProjectSettings
import com.chimerapps.niddler.ui.util.execution.tool.AndroidQuickConnectHelper
import com.chimerapps.niddler.ui.util.ui.ensureMain
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowManager
import java.util.regex.Pattern

class ProcessExecutionListener(val project: Project) {

    init {
        project.messageBus.connect(project).subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
                super.processStarted(executorId, env, handler)
                onProcessStarted(env, handler);
            }

        })
    }

    private fun onProcessStarted(env: ExecutionEnvironment, handler: ProcessHandler) {
        val settings = NiddlerProjectSettings.instance(project)
        if (settings.automaticallyReconnect != true) {
            return
        }

        handler.addProcessListener(NiddlerProcessListener(project, env, handler,
                settings.reuseSession == true, settings.connectUsingDebugger == true))
    }

    private class NiddlerProcessListener(private val project: Project,
                                         executionEnvironment: ExecutionEnvironment,
                                         handler: ProcessHandler,
                                         private val reuseSession: Boolean,
                                         private val connectUsingDebugger: Boolean) : ProcessListener {

        private val androidHelper = AndroidQuickConnectHelper(executionEnvironment, handler, project)

        private val matcher = Pattern.compile(NiddlerConnectFilter.START_REGEX).matcher("")

        override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {
            synchronized(matcher) {
                matcher.reset(p0.text)
                if (matcher.matches()) {
                    val port = matcher.group(1).toInt()
                    val tag = matcher.group(2)
                    connectNiddler(port, tag)
                }
            }
        }

        override fun processTerminated(p0: ProcessEvent) {
        }

        override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {
        }

        override fun startNotified(p0: ProcessEvent) {
        }

        private fun connectNiddler(port: Int, tag: String) {
            val info = getQuickConnectionInfo(port, tag)

            ensureMain {
                val window = ToolWindowManager.getInstance(project).getToolWindow("Niddler") ?: return@ensureMain
                val niddlerWindow = window.contentManager.getContent(0)?.component as? NiddlerToolWindow ?: return@ensureMain

                if (!window.isVisible) {
                    niddlerWindow.newSessionFor(info, reuse = reuseSession, connectUsingDebugger = connectUsingDebugger)
                } else {
                    niddlerWindow.newSessionFor(info, reuse = reuseSession, connectUsingDebugger = connectUsingDebugger)
                }
            }
        }

        private fun getQuickConnectionInfo(port: Int, tag: String): QuickConnectionInfo {
            androidHelper.getQuickConnectionInfo(port, tag)?.let {
                return it
            }

            return QuickConnectionInfo(port, tag)
        }
    }

}