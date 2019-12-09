package com.chimerapps.discovery.device.adb

import com.chimerapps.discovery.utils.Platform
import com.chimerapps.discovery.utils.currentPlatform
import com.chimerapps.discovery.utils.debug
import com.chimerapps.discovery.utils.info
import com.chimerapps.discovery.utils.logger
import com.chimerapps.discovery.utils.warn
import se.vidstige.jadb.JadbConnection
import java.io.File
import java.util.ArrayList
import java.util.HashSet
import java.util.concurrent.TimeUnit

/**
 * @author Nicola Verbeeck
 */
class ADBBootstrap(sdkPathGuesses: Collection<String>, private val adbPathProvider: (() -> String?)? = null) {

    companion object {
        private val log = logger<ADBBootstrap>()
        private const val DEFAULT_ADB_TIMEOUT_S = 4L
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

        private fun findADB(sdkPathGuesses: Collection<String>): String? {
            findAndroidSdkDirs(sdkPathGuesses).forEach {
                val file = hasValidAdb(it)
                if (file != null)
                    return file.absolutePath
            }
            log.info("Failed to find android sdk dir, searching path")

            return determineExecutablePath("adb$platformExt")
        }

        private fun hasValidAdb(sdkDir: String): File? {
            log.debug("Found android sdk at $sdkDir")
            val adbFile = File("$sdkDir${File.separator}platform-tools${File.separator}adb$platformExt")
            if (adbFile.exists() && adbFile.canExecute())
                return adbFile

            log.warn("Could not find adb at $adbFile -> Exist: ${adbFile.exists()}, Can execute: ${adbFile.canExecute()}")
            return null
        }

        private fun findAndroidSdkDirs(sdkPathGuesses: Collection<String>): Collection<String> {
            val list = HashSet<String>(sdkPathGuesses)
            val env = System.getenv("ANDROID_HOME")
            if (env != null) {
                log.info("Got android sdk dir from env variable: $env")
                list += env
            }
            return list
        }

    }

    val pathToAdb: String? = adbPathProvider?.invoke() ?: findADB(sdkPathGuesses)
    private var hasBootStrap = false

    fun bootStrap(): ADBInterface {
        if (!hasBootStrap) {
            hasBootStrap = true
            executeADBCommand("start-server")
        }
        if (pathToAdb == null)
            return ADBInterface(connection = null, bootstrap = this)
        return ADBInterface(connection = JadbConnection(), bootstrap = this)
    }

    fun executeADBCommand(vararg commands: String) = executeADBCommand(DEFAULT_ADB_TIMEOUT_S, *commands)

    fun executeADBCommand(timeoutInSeconds: Long, vararg commands: String): String? {
        return adbPathProvider?.invoke() ?: pathToAdb?.let {
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