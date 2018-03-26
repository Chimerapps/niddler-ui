package com.icapps.niddler.lib.adb

import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.info
import com.icapps.niddler.lib.utils.logger
import com.icapps.niddler.lib.utils.warn
import se.vidstige.jadb.JadbConnection
import java.io.File
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
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
            if (currentPlatform() == PLATFORM_WINDOWS) {
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

        private const val PLATFORM_UNKNOWN = 0
        private const val PLATFORM_LINUX = 1
        private const val PLATFORM_WINDOWS = 2
        private const val PLATFORM_DARWIN = 3

        private fun currentPlatform(): Int {
            val os = System.getProperty("os.name")
            return when {
                os.startsWith("Mac OS") -> PLATFORM_DARWIN
                os.startsWith("Windows") -> PLATFORM_WINDOWS
                os.startsWith("Linux") -> PLATFORM_LINUX
                else -> PLATFORM_UNKNOWN
            }
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
            process.waitFor()
            val response = process.inputStream.bufferedReader().readText().trim()
            val error = process.errorStream.bufferedReader().readText()
            if (error.isNotBlank())
                System.err.println(error)
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