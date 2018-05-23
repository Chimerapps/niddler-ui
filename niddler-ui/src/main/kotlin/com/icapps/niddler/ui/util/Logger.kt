package com.icapps.niddler.ui.util

import com.icapps.niddler.lib.utils.Logger
import com.icapps.niddler.lib.utils.LoggerFactory
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * @author Nicola Verbeeck
 * @date 25/04/2017.
 */

class JavaLogFactory : LoggerFactory {

    override fun getInstanceForClass(clazz: Class<*>): Logger {
        val logger = java.util.logging.Logger.getLogger(clazz.name)
        return LoggerWrapper(logger)
    }

}

private class LoggerWrapper(private val instance: java.util.logging.Logger) : Logger {

    override fun doLogDebug(message: String?, t: Throwable?) {
        val record = LogRecord(Level.FINE, message)
        record.thrown = t
        record.sourceClassName = instance.name
        record.loggerName = instance.name
        record.sourceMethodName = inferMethod()
        instance.log(record)
    }

    override fun doLogInfo(message: String?, t: Throwable?) {
        val record = LogRecord(Level.INFO, message)
        record.thrown = t
        record.sourceClassName = instance.name
        record.loggerName = instance.name
        record.sourceMethodName = inferMethod()
        instance.log(record)
    }

    override fun doLogWarn(message: String?, t: Throwable?) {
        val record = LogRecord(Level.WARNING, message)
        record.thrown = t
        record.sourceClassName = instance.name
        record.loggerName = instance.name
        record.sourceMethodName = inferMethod()
        instance.log(record)
    }

    override fun doLogError(message: String?, t: Throwable?) {
        val record = LogRecord(Level.SEVERE, message)
        record.thrown = t
        record.sourceClassName = instance.name
        record.loggerName = instance.name
        record.sourceMethodName = inferMethod()
        instance.log(record)
    }

    private fun inferMethod(): String? {
        val ex = Throwable()
        return ex.stackTrace.getOrNull(4)?.let {
            "${it.methodName}:${it.lineNumber}"
        }
    }

}