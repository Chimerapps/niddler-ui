package com.chimerapps.discovery.utils

import com.chimerapps.discovery.device.idevice.IDeviceCommand
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.streams.toList

internal class IDeviceCommandExecutor(private val binaryPath: File) {

    fun <T> execute(command: IDeviceCommand<T>): Future<T> {
        return when (currentPlatform) {
            Platform.WINDOWS, Platform.LINUX, Platform.UNKNOWN -> {
                TODO("Not supported on this platform")
            }
            Platform.DARWIN -> executeUsingShell(command)
        }
    }

    private fun <T> executeUsingShell(command: IDeviceCommand<T>): Future<T> {
        val builder = ProcessBuilder()
        builder.command().add(File(binaryPath, command.command).absolutePath)
        builder.command().addAll(command.arguments)

        val future = CompletableFuture<T>()

        val process = builder.start()
        command.onProcessStarted(process) { resultCode ->
            val stdOut = process.inputStream.bufferedReader().lines().toList()
            val stdErr = process.errorStream.bufferedReader().lines().toList()

            future.complete(command.handle(this, resultCode, stdOut, stdErr))
        }
        return future
    }

}