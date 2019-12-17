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
        val hook = Thread {
            try {
                process.destroy()
            } catch (e: Throwable) {
            }
        }
        Runtime.getRuntime().addShutdownHook(hook)
        try {
            command.onProcessStarted(process) { resultCode ->
                try {
                    val stdOut = process.inputStream.bufferedReader().lines().toList()
                    val stdErr = process.errorStream.bufferedReader().lines().toList()

                    Runtime.getRuntime().removeShutdownHook(hook)
                    future.complete(command.handle(this, resultCode, stdOut, stdErr))
                } catch (e: Throwable) {
                }
            }
        } catch (e: Throwable) {
            try {
                process.destroy()
            } catch (e: Throwable) {
            }
            Runtime.getRuntime().removeShutdownHook(hook)
        }
        return future
    }

}