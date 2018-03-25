import com.icapps.niddler.lib.adb.ADBBootstrap
import com.icapps.niddler.lib.adb.ADBInterface
import com.icapps.niddler.lib.utils.LoggerFactory
import org.junit.Before
import org.junit.Test

/**
 * @author nicolaverbeeck
 */
class ADBInterfaceListenerTest {

    @Before
    fun setUp() {
        LoggerFactory.instance = SimpleLogger.SimpleLoggerFactory()
    }

    @Test
    fun testListenForDevice() {
        val boostrap = ADBBootstrap(emptyList())
        val adbInterface = boostrap.bootStrap()
        val cancellable = adbInterface.createDeviceWatcher {
            println("Devices changed")
            listDevices(it)
        }
        listDevices(adbInterface)
        System.`in`.read()
        cancellable.cancel()
    }

    private fun listDevices(adbInterface: ADBInterface) {
        println("-----Devices-----")
        adbInterface.devices.forEach {
            println("Device - ${it.serial}")
        }
    }

}