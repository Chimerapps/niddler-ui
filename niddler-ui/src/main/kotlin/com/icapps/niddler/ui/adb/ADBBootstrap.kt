package com.icapps.niddler.ui.adb

import com.icapps.niddler.ui.prefixList
import com.icapps.niddler.ui.util.logger
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
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

        private val PLATFORM_UNKNOWN = 0
        private val PLATFORM_LINUX = 1
        private val PLATFORM_WINDOWS = 2
        private val PLATFORM_DARWIN = 3

        private fun currentPlatform(): Int {
            val os = System.getProperty("os.name")
            if (os.startsWith("Mac OS")) {
                return PLATFORM_DARWIN
            } else if (os.startsWith("Windows")) {
                return PLATFORM_WINDOWS
            } else if (os.startsWith("Linux")) {
                return PLATFORM_LINUX
            }

            return PLATFORM_UNKNOWN
        }

    }

    private var pathToAdb: String? = findADB(sdkPathGuesses)
    private var hasBootStrap = false

    fun bootStrap(): JadbConnection {
        if (!hasBootStrap) {
            hasBootStrap = true
            executeADBCommand("start-server")
        }
        if (pathToAdb == null) {
            return object : JadbConnection() {
                override fun createDeviceWatcher(listener: DeviceDetectionListener?): DeviceWatcher? {
                    return null
                }

                override fun getHostVersion() {
                }

                override fun parseDevices(body: String?): MutableList<JadbDevice> {
                    return mutableListOf()
                }

                override fun getDevices(): MutableList<JadbDevice> {
                    return mutableListOf()
                }

                override fun getAnyDevice(): JadbDevice? {
                    return null
                }
            }
        }
        return JadbConnection()
    }

    fun extend(device: JadbDevice?): ADBExt? {
        if (device == null || pathToAdb == null) return null
        return extend(device.serial)
    }

    fun extend(serial: String?): ADBExt? {
        if (pathToAdb == null) return null
        return ADBExt(serial, this)
    }

    internal fun executeADBCommand(vararg commands: String): String? {
        return pathToAdb?.let {
            val builder = ProcessBuilder(it.prefixList(commands))
            val process = builder.start()
            process.waitFor()
            val response = process.inputStream.bufferedReader().readText().trim()
            println(response)
            System.err.println(process.errorStream.bufferedReader().readText())
            response
        }
    }
}
