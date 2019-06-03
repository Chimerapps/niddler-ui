package com.icapps.niddler.lib.utils

/**
 * @author nicolaverbeeck
 */
interface Logger {

    fun doLogDebug(message: String?, t: Throwable?)

    fun doLogInfo(message: String?, t: Throwable?)

    fun doLogWarn(message: String?, t: Throwable?)

    fun doLogError(message: String?, t: Throwable?)

}

interface LoggerFactory {

    companion object {
        var instance: LoggerFactory? = null

        fun getInstance(clazz: Class<*>): Logger? {
            return instance?.getInstanceForClass(clazz)
        }
    }

    fun getInstanceForClass(clazz: Class<*>): Logger?

}

inline fun <reified T> logger(): Logger? {
    return LoggerFactory.getInstance(T::class.java)
}

fun Logger?.debug(message: String?, t: Throwable? = null) {
    this?.doLogDebug(message, t)
}

fun Logger?.info(message: String?, t: Throwable? = null) {
    this?.doLogInfo(message, t)
}

fun Logger?.warn(message: String?, t: Throwable? = null) {
    this?.doLogWarn(message, t)
}

fun Logger?.error(message: String?, t: Throwable? = null) {
    this?.doLogError(message, t)
}