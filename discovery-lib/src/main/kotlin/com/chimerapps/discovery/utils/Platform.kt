package com.chimerapps.discovery.utils

import java.net.InetAddress

/**
 * @author Nicola Verbeeck
 */

enum class Platform {
    UNKNOWN, LINUX, WINDOWS, DARWIN
}

val currentPlatform: Platform by lazy {
    val os = System.getProperty("os.name")
    when {
        os.startsWith("Mac OS", ignoreCase = true) -> Platform.DARWIN
        os.startsWith("Windows", ignoreCase = true) -> Platform.WINDOWS
        os.startsWith("Linux", ignoreCase = true) -> Platform.LINUX
        else -> Platform.UNKNOWN
    }
}

val localName: String? by lazy {
    try {
        when (currentPlatform) {
            Platform.UNKNOWN,
            Platform.DARWIN,
            Platform.LINUX -> System.getenv("HOSTNAME")
            Platform.WINDOWS -> System.getenv("COMPUTERNAME")
        } ?: InetAddress.getLocalHost().hostName
    } catch (e: Throwable) {
        logger<Platform>().debug("Failed to get host name", e)
        null
    }
}