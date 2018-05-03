package com.icapps.niddler.ui.util


import java.lang.reflect.Method
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Nicola Verbeeck
 * @date 25/04/2017.
 */
abstract class LoggerRt {

    private interface Factory {
        fun getInstance(category: String): LoggerRt
    }

    fun debug(message: String?) {
        debug(message, null)
    }

    fun info(message: String?) {
        info(message, null)
    }

    fun info(t: Throwable) {
        info(t.message, t)
    }

    fun warn(message: String?) {
        warn(message, null)
    }

    fun warn(t: Throwable) {
        warn(t.message, t)
    }

    fun error(message: String?) {
        error(message, null)
    }

    fun error(t: Throwable) {
        error(t.message, t)
    }

    abstract fun debug(message: String?, t: Throwable?)
    abstract fun info(message: String?, t: Throwable?)
    abstract fun warn(message: String?, t: Throwable?)
    abstract fun error(message: String?, t: Throwable?)

    private class JavaFactory : Factory {
        override fun getInstance(category: String): LoggerRt {
            val logger = Logger.getLogger(category)
            return object : LoggerRt() {
                override fun info(message: String?, t: Throwable?) {
                    logger.log(Level.INFO, message, t)
                }

                override fun warn(message: String?, t: Throwable?) {
                    logger.log(Level.WARNING, message, t)
                }

                override fun error(message: String?, t: Throwable?) {
                    logger.log(Level.SEVERE, message, t)
                }

                override fun debug(message: String?, t: Throwable?) {
                    logger.log(Level.FINE, message, t)
                }
            }
        }
    }

    private class IdeaFactory @Throws(Exception::class) constructor() : Factory {
        private val myGetInstance: Method
        private val myDebug: Method
        private val myInfo: Method
        private val myWarn: Method
        private val myError: Method

        init {
            val loggerClass = Class.forName("com.intellij.openapi.diagnostic.Logger")
            myGetInstance = loggerClass.getMethod("getInstance", String::class.java)
            myGetInstance.isAccessible = true
            myDebug = loggerClass.getMethod("debug", String::class.java, Throwable::class.java)
            myDebug.isAccessible = true
            myInfo = loggerClass.getMethod("info", String::class.java, Throwable::class.java)
            myInfo.isAccessible = true
            myWarn = loggerClass.getMethod("warn", String::class.java, Throwable::class.java)
            myInfo.isAccessible = true
            myError = loggerClass.getMethod("error", String::class.java, Throwable::class.java)
            myError.isAccessible = true
        }

        override fun getInstance(category: String): LoggerRt {
            try {
                val logger = myGetInstance.invoke(null, category)
                return object : LoggerRt() {
                    override fun debug(message: String?, t: Throwable?) {
                        try {
                            myDebug.invoke(logger, message, t)
                        } catch (ignored: Exception) {
                        }
                    }

                    override fun info(message: String?, t: Throwable?) {
                        try {
                            myInfo.invoke(logger, message, t)
                        } catch (ignored: Exception) {
                        }

                    }

                    override fun warn(message: String?, t: Throwable?) {
                        try {
                            myWarn.invoke(logger, message, t)
                        } catch (ignored: Exception) {
                        }

                    }

                    override fun error(message: String?, t: Throwable?) {
                        try {
                            myError.invoke(logger, message, t)
                        } catch (ignored: Exception) {
                        }

                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
    }

    companion object {

        private var ourFactory: Factory? = null

        private val factory: Factory
            @Synchronized get() {
                if (ourFactory == null) {
                    try {
                        ourFactory = IdeaFactory()
                    } catch (t: Throwable) {
                        ourFactory = JavaFactory()
                    }
                }
                return ourFactory!!
            }

        fun getInstance(category: String): LoggerRt {
            return factory.getInstance(category)
        }

        fun getInstance(clazz: Class<*>): LoggerRt {
            return getInstance('#' + clazz.name)
        }
    }
}

inline fun <reified T> logger(): LoggerRt {
    return LoggerRt.getInstance(T::class.java)
}