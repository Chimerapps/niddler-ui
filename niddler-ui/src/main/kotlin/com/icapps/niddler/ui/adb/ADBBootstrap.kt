package com.icapps.niddler.ui.adb

import com.icapps.niddler.ui.prefixList
import com.icapps.niddler.ui.util.logger
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import java.io.File

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
class ADBBootstrap {

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

        private fun findADB(): String? {
            val androidHomePath = System.getenv("ANDROID_HOME")
            if (androidHomePath != null) {
                log.debug("Found android home at $androidHomePath")
                val adbFile = File("$androidHomePath${File.separator}platform-tools${File.separator}adb${ext(".exe", "")}")
                if (adbFile.exists() && adbFile.canExecute())
                    return adbFile.absolutePath
                else
                    log.warn("Could not find adb at $adbFile -> Exist: ${adbFile.exists()}, Can execute: ${adbFile.canExecute()}")
            } else {
                log.info("Failed to find ANDROID_HOME environment variable. Variables:")
                System.getenv().forEach {
                    log.info("${it.key} = ${it.value}")
                }
            }
            return determineExecutablePath("adb")
        }

        private fun ext(windowsExtension: String, nonWindowsExtension: String): String {
            if (currentPlatform() == PLATFORM_WINDOWS) {
                return windowsExtension
            } else {
                return nonWindowsExtension
            }
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

    private var pathToAdb: String? = findADB()
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

    internal fun executeADBCommand(vararg commands: String) {
        if (pathToAdb == null)
            return
        val builder = ProcessBuilder(pathToAdb?.prefixList(commands))
        val process = builder.start()
        process.waitFor()
        print(process.inputStream.bufferedReader().readText())
        System.err.println(process.errorStream.bufferedReader().readText())
    }

}
