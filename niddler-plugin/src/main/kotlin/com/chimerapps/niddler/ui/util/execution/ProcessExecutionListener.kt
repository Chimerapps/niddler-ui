package com.chimerapps.niddler.ui.util.execution

import com.chimerapps.niddler.ui.NiddlerToolWindow
import com.chimerapps.niddler.ui.PIDQuickConnectionInfo
import com.chimerapps.niddler.ui.QuickConnectionInfo
import com.chimerapps.niddler.ui.provider.NiddlerConnectFilter
import com.chimerapps.niddler.ui.settings.NiddlerProjectSettings
import com.chimerapps.niddler.ui.util.execution.tool.AndroidQuickConnectHelper
import com.chimerapps.niddler.ui.util.execution.tool.DeviceWithProcessId
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
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
import java.util.Timer
import java.util.regex.Pattern
import kotlin.concurrent.schedule

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

        handler.addProcessListener(
            NiddlerProcessListener(
                project = project,
                executionEnvironment = env,
                handler = handler,
                reuseSession = settings.reuseSession == true,
                connectUsingDebugger = settings.connectUsingDebugger == true,
                isAndroidDebug = isAndroidDebug,
                logDebug = settings.logDebugInfo == true,
            )
        )
    }

    private class NiddlerProcessListener(
        private val project: Project,
        executionEnvironment: ExecutionEnvironment,
        handler: ProcessHandler,
        private val reuseSession: Boolean,
        private val connectUsingDebugger: Boolean,
        private val isAndroidDebug: Boolean,
        private val logDebug: Boolean,
    ) : ProcessListener {

        private companion object {
            private const val CONNECTED_DEBUGGER_REGEX = "Connected to process (\\d+) on device.*" //TODO also figure out device info
        }

        private val androidHelper = AndroidQuickConnectHelper(executionEnvironment, handler, project)
        private val debuggerMatcher = Pattern.compile(CONNECTED_DEBUGGER_REGEX).matcher("")
        private var foundServerInfo = false
        private var processStartDetails: DeviceWithProcessId? = null
        private var didNotFindUsingTextTimer = Timer("Auto start delay", false)

        init {
            if (logDebug) {
                NotificationUtil.debug(
                    "Created process listener",
                    "isDebug: $isAndroidDebug, connect debugger: $connectUsingDebugger, reuse session: $reuseSession",
                    project,
                )
            }
        }

        override fun onTextAvailable(p0: ProcessEvent, p1: Key<*>) {
            synchronized(didNotFindUsingTextTimer) {
                if (!foundServerInfo) {
                    checkIfStarted()
                    val serverInfo = NiddlerConnectFilter.findServerStart(p0.text)
                    if (serverInfo != null) {
                        processStartDetails = null
                        foundServerInfo = true
                        didNotFindUsingTextTimer.cancel()

                        val waitingForDebugger = serverInfo.extras["waitingForDebugger"] == "true"

                        if (logDebug) {
                            NotificationUtil.debug(
                                "Found niddler startup message",
                                "Running on port: ${serverInfo.port}, with tag: ${serverInfo.tag}, running with debugger: $waitingForDebugger",
                                project,
                            )
                        }

                        connectNiddler(serverInfo.port, serverInfo.tag, waitingForDebugger)
                    }
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

        private fun checkIfStarted() {
            if (processStartDetails != null) return
            if (!AndroidQuickConnectHelper.isAndroidSupported) return

            val details = androidHelper.getDeviceProcessInfo()
            if (logDebug) {
                NotificationUtil.debug(
                    "Process listener",
                    "Process started, did we determine process info? ${details?.deviceSerial}, pid: ${details?.processId}",
                    project,
                )
            }
            if (details != null) {
                processStartDetails = details
                didNotFindUsingTextTimer.schedule(3000L) {
                    synchronized(didNotFindUsingTextTimer) {
                        if (!foundServerInfo) {
                            if (logDebug) {
                                NotificationUtil.debug(
                                    "Process listener",
                                    "Timeout waiting for the niddler message, using direct connection to device at pid",
                                    project,
                                )
                            }

                            foundServerInfo = true
                            val safeDetails = processStartDetails ?: return@schedule
                            NiddlerAutomaticConnectionHelper.connect(
                                project = project,
                                info = PIDQuickConnectionInfo(safeDetails.deviceSerial, safeDetails.processId),
                                reuseSession = reuseSession,
                                connectUsingDebugger = connectUsingDebugger,
                            )
                        }
                    }
                }
            }
        }

        override fun processTerminated(p0: ProcessEvent) {
            didNotFindUsingTextTimer.cancel()
        }

        override fun processWillTerminate(p0: ProcessEvent, p1: Boolean) {
            processStartDetails = null
            didNotFindUsingTextTimer.cancel()
        }

        override fun startNotified(p0: ProcessEvent) {
            checkIfStarted()
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

    fun connect(project: Project, info: PIDQuickConnectionInfo, reuseSession: Boolean, connectUsingDebugger: Boolean) {
        ensureMain {
            val (niddlerWindow, _) = NiddlerToolWindow.get(project) ?: return@ensureMain

            niddlerWindow.newSessionFor(info, reuse = reuseSession, connectUsingDebugger = connectUsingDebugger)
        }
    }
}