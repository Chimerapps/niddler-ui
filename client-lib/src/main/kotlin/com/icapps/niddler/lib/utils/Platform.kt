package com.icapps.niddler.lib.utils

import java.net.InetAddress

/**
 * @author nicolaverbeeck
 */

enum class Platform {
    UNKNOWN, LINUX, WINDOWS, DARWIN
}

fun currentPlatform(): Platform {
    val os = System.getProperty("os.name")
    return when {
        os.startsWith("Mac OS", ignoreCase = true) -> Platform.DARWIN
        os.startsWith("Windows", ignoreCase = true) -> Platform.WINDOWS
        os.startsWith("Linux", ignoreCase = true) -> Platform.LINUX
        else -> Platform.UNKNOWN
    }
}

fun localName(): String? {
    try {
        return when (currentPlatform()) {
            Platform.UNKNOWN,
            Platform.DARWIN,
            Platform.LINUX -> System.getenv("HOSTNAME") ?: InetAddress.getLocalHost().hostName
            Platform.WINDOWS -> System.getenv("COMPUTERNAME")
        }
    } catch (e: Throwable) {
        logger<Platform>().debug("Failed to get host name", e)
        return null
    }
}