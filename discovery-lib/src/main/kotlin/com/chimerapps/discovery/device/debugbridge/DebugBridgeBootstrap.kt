package com.chimerapps.discovery.device.debugbridge

import com.chimerapps.discovery.utils.Platform
import com.chimerapps.discovery.utils.currentPlatform
import com.chimerapps.discovery.utils.debug
import com.chimerapps.discovery.utils.info
import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author Nicola Verbeeck
 */
open class DebugBridgeBootstrap(
    private val toolExecutableName: String,
    private val toolName: String,
    private val sdkPathEnvironmentVariable: String,
    private val sdkToolSubDirectory: String,
    private val sdkPathGuesses: Collection<String>,
    private val debugBridgePathProvider: (() -> String?)? = null,
    private val deviceBridgeServerPort: Int,
    private val deviceCreator: (JadbDevice, DebugBridgeBootstrap) -> DebugBridgeBasedDevice,
) {

    companion object {
        private val log = logger<DebugBridgeBootstrap>()

        private const val DEFAULT_DEBUG_BRIDGE_TIMEOUT_S = 4L

        private val platformExt: String by lazy { if (currentPlatform == Platform.WINDOWS) ".exe" else "" }

        private fun determineExecutablePath(name: String): String? {
            val foundPath = System.getenv("PATH").split(File.pathSeparator).find {
                val file = File(it, name)
                file.exists() && file.canExecute()
            }
            if (foundPath != null)
                return "$foundPath${File.separator}$name"
            return null
        }

    }

    private fun findDebugBridge(sdkPathGuesses: Collection<String>): String? {
        findSdkDirs(sdkPathGuesses).forEach {
            val file = hasValidDebugBridge(it)
            if (file != null)
                return file.absolutePath
        }
        log.info("Failed to find $toolName sdk dir, searching path")

        return determineExecutablePath("$toolExecutableName$platformExt")
    }

    private fun hasValidDebugBridge(sdkDir: String): File? {
        log.debug("Found $toolName sdk at $sdkDir")
        val debugBridgeFile = File("$sdkDir${File.separator}$sdkToolSubDirectory${File.separator}$toolExecutableName$platformExt")
        if (debugBridgeFile.exists() && debugBridgeFile.canExecute())
            return debugBridgeFile

        log.warn("Could not find $toolExecutableName at $debugBridgeFile -> Exist: ${debugBridgeFile.exists()}, Can execute: ${debugBridgeFile.canExecute()}")
        return null
    }

    private fun findSdkDirs(sdkPathGuesses: Collection<String>): Collection<String> {
        val list = HashSet(sdkPathGuesses)
        val env = System.getenv(sdkPathEnvironmentVariable)
        if (env != null) {
            log.info("Got $toolName sdk dir from env variable: $env ($sdkPathEnvironmentVariable)")
            list += env
        }
        return list
    }


    val pathToDebugBridge: String?
        get() = debugBridgePathProvider?.invoke() ?: findDebugBridge(sdkPathGuesses)

    private var hasBootStrap = false

    fun bootStrap(): DebugBridgeInterface {
        val pathToDebugBridge = pathToDebugBridge
        if (!hasBootStrap && pathToDebugBridge != null && File(pathToDebugBridge).let { it.exists() && it.canExecute() }) {
            try {
                executeDebugBridgeCommand("start-server")
                hasBootStrap = true
            } catch (e: Throwable) {
            }
        }
        if (pathToDebugBridge == null || !File(pathToDebugBridge).let { it.exists() && it.canExecute() })
            return DebugBridgeInterface(connection = null, bootstrap = this, deviceCreator = deviceCreator)
        return DebugBridgeInterface(
            connection = JadbConnection("localhost", deviceBridgeServerPort),
            bootstrap = this,
            deviceCreator = deviceCreator,
        )
    }

    fun executeDebugBridgeCommand(vararg commands: String) = executeDebugBridgeCommand(DEFAULT_DEBUG_BRIDGE_TIMEOUT_S, *commands)

    fun executeDebugBridgeCommand(timeoutInSeconds: Long, vararg commands: String): String? {
        return (debugBridgePathProvider?.invoke() ?: pathToDebugBridge)?.let {
            val builder = ProcessBuilder(it.prepend(commands))
            val process = builder.start()
            val success = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS)
            val response = if (success) {
                val response = process.inputStream.bufferedReader().readText().trim()
                val error = process.errorStream.bufferedReader().readText()
                if (error.isNotBlank())
                    System.err.println(error)
                response
            } else
                null
            try {
                if (!success)
                    process.destroy()
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            response
        }
    }

}

private fun String.prepend(list: Array<out String>): List<String> {
    val newList = ArrayList<String>(list.size + 1)
    newList += this
    newList += list
    return newList
}