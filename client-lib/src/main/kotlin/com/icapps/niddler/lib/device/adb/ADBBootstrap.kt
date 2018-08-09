package com.icapps.niddler.lib.device.adb

import com.icapps.niddler.lib.utils.Platform
import com.icapps.niddler.lib.utils.currentPlatform
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.info
import com.icapps.niddler.lib.utils.logger
import com.icapps.niddler.lib.utils.warn
import se.vidstige.jadb.JadbConnection
import java.io.File
import java.util.ArrayList
import java.util.HashSet
import java.util.concurrent.TimeUnit

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
private const val ADB_TIMEOUT_S = 2L

class ADBBootstrap(sdkPathGuesses: Collection<String>) {

    companion object {
        private val log = logger<ADBBootstrap>()

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

            return determineExecutablePath("adb" + ext(".exe", ""))
        }

        private fun ext(windowsExtension: String, nonWindowsExtension: String): String {
            if (currentPlatform() == Platform.WINDOWS) {
                return windowsExtension
            } else {
                return nonWindowsExtension
            }
        }

        private fun hasValidAdb(sdkDir: String): File? {
            log.debug("Found android sdk at $sdkDir")
            val adbFile = File("$sdkDir${File.separator}platform-tools${File.separator}adb${ext(".exe", "")}")
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

    private var pathToAdb: String? = findADB(sdkPathGuesses)
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

    fun executeADBCommand(vararg commands: String): String? {
        return pathToAdb?.let {
            val builder = ProcessBuilder(it.prepend(commands))
            val process = builder.start()
            val success = process.waitFor(ADB_TIMEOUT_S, TimeUnit.SECONDS)
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