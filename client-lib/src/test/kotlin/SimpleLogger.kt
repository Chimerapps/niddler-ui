import com.icapps.niddler.lib.utils.Logger
import com.icapps.niddler.lib.utils.LoggerFactory

/**
 * @author nicolaverbeeck
 */
class SimpleLogger(private val name: String) : Logger {

    override fun doLogDebug(message: String?, t: Throwable?) {
        println("DEBUG - $name - $message")
        t?.printStackTrace()
    }

    override fun doLogInfo(message: String?, t: Throwable?) {
        println("INFO - $name - $message")
        t?.printStackTrace()
    }

    override fun doLogWarn(message: String?, t: Throwable?) {
        println("WARN - $name - $message")
        t?.printStackTrace()
    }

    override fun doLogError(message: String?, t: Throwable?) {
        println("ERROR - $name - $message")
        t?.printStackTrace()
    }

    class SimpleLoggerFactory : LoggerFactory {

        override fun getInstanceForClass(clazz: Class<*>): Logger? {
            return SimpleLogger(clazz.simpleName)
        }

    }

}