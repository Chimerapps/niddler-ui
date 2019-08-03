package com.chimerapps.discovery.utils

import java.io.IOException
import java.net.ServerSocket

@Suppress("unused", "LiftReturnOrAssignment")
inline fun <reified T> T.freePort(): Int {
    try {
        val serverSocket = ServerSocket(0)
        val freePort = serverSocket.localPort
        serverSocket.close()
        return freePort
    } catch (e: IOException) {
        logger<T>().error("Failed to find free port:", e)
        return 0
    }
}