package com.chimerapps.niddler.ui.util.execution

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.QuickConnectionInfo
import com.chimerapps.niddler.ui.provider.NiddlerConnectFilter
import com.chimerapps.niddler.ui.settings.NiddlerProjectSettings
import com.chimerapps.niddler.ui.util.execution.tool.AndroidQuickConnectHelper
import com.chimerapps.niddler.ui.util.ui.ensureMain
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowManager
import java.util.regex.Pattern

class ProcessExecutionListener(val project: Project) {

    companion object {
        val autoConnectHelper = mutableSetOf<Int>()
    }

    init {
        project.messageBus.connect(project).subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
                super.processStarted(executorId, env, handler)
                val isAndroidDebug = env.executor is DefaultDebugExecutor

                onProcessStarted(env, handler, isAndroidDebug = isAndroidDebug)
            }

        })
    }

    private fun onProcessStarted(env: ExecutionEnvironment, handler: ProcessHandler, isAndroidDebug: Boolean) {
        val settings = NiddlerProjectSettings.instance(project)
        if (settings.automaticallyReconnect != true) {
            return
        }

        handler.addProcessListener(NiddlerProcessListener(
                project = project,
                executionEnvironment = env,
                handler = handler,
                reuseSession = settings.reuseSession == true,
                connectUsingDebugger = settings.connectUsingDebugger == true,
                isAndroidDebug = isAndroidDebug
        ))
    }

    private class NiddlerProcessListener(private val project: Project,
                                         executionEnvironment: ExecutionEnvironment,
                                         handler: ProcessHandler,
                                         private val reuseSession: Boolean,
                                         private val connectUsingDebugger: Boolean,
                                         private val isAndroidDebug: Boolean) : ProcessListener {

        private companion object {
            private const val CONNECTED_DEBUGGER_REGEX = "Connected to process (\\d+) on device.*" //TODO also figure out device info
        }

        private val androidHelper = AndroidQuickConnectHelper(executionEnvironment, handler, project)

        private val matcher = Pattern.compile(NiddlerConnectFilter.START_REGEX).matcher("")
        private val debuggerMatcher = Pattern.compile(CONNECTED_DEBUGGER_REGEX).matcher("")

        override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {
            synchronized(matcher) {
                if (matcher.reset(p0.text).matches()) {
                    val port = matcher.group(1).toInt()
                    val tag = matcher.group(2)
                    val waitingForDebugger = if (matcher.groupCount() >= 4) { (matcher.group(4) == "true") } else false

                    connectNiddler(port, tag, waitingForDebugger)
                }
            }
            if (isAndroidDebug) {
                synchronized(debuggerMatcher) {
                    if (debuggerMatcher.reset(p0.text.trim()).matches()) {
                        val processId = debuggerMatcher.group(1).toInt()
                        autoConnectHelper.add(processId)
                    }
                }
            }
        }

        override fun processTerminated(p0: ProcessEvent) {
        }

        override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {
        }

        override fun startNotified(p0: ProcessEvent) {
        }

        private fun connectNiddler(port: Int, tag: String, waitingForDebugger: Boolean) {
            val info = getQuickConnectionInfo(port, tag)

            NiddlerAutomaticConnectionHelper.connect(
                    project = project,
                    info = info,
                    reuseSession = reuseSession,
                    connectUsingDebugger = connectUsingDebugger || waitingForDebugger
            )
        }

        private fun getQuickConnectionInfo(port: Int, tag: String): QuickConnectionInfo {
            androidHelper.getQuickConnectionInfo(port, tag)?.let {
                return it
            }

            return QuickConnectionInfo(port, tag)
        }
    }

}

object NiddlerAutomaticConnectionHelper {

    fun connect(project: Project, info: QuickConnectionInfo, reuseSession: Boolean, connectUsingDebugger: Boolean) {
        ensureMain {
            val (niddlerWindow, _) = NiddlerToolWindow.get(project) ?: return@ensureMain

            niddlerWindow.newSessionFor(info, reuse = reuseSession, connectUsingDebugger = connectUsingDebugger)
        }
    }

}