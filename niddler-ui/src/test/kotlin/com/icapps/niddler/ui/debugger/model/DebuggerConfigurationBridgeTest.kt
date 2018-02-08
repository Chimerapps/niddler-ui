package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.debugger.model.saved.DebuggerConfigurationProvider
import com.icapps.niddler.ui.debugger.model.saved.DisableableItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author nicolaverbeeck
 */
class DebuggerConfigurationBridgeTest {

    private lateinit var configurationProvider: DebuggerConfigurationProvider
    private lateinit var debuggerInterface: DebuggerInterface
    private lateinit var bridge: DebuggerConfigurationBridge

    @Before
    fun setUp() {
        configurationProvider = mockk()
        debuggerInterface = mockk(relaxed = true)
        bridge = DebuggerConfigurationBridge(configurationProvider, debuggerInterface)
    }

    @Test
    fun apply() {
        val activeDefaultResponse = DefaultResponseAction(null, false, "reg1", null, DebugResponse(200, "OK", null, null, null))
        every { configurationProvider.delayConfiguration } returns DisableableItem(enabled = true, item = DebuggerDelays(123, 456, 789))
        every { configurationProvider.blacklistConfiguration } returns listOf(DisableableItem(enabled = true, item = "reg1"),
                DisableableItem(enabled = false, item = "reg2"))
        every { configurationProvider.defaultResponses } returns listOf(DisableableItem(true, activeDefaultResponse),
                DisableableItem(false, DefaultResponseAction(null, false, "reg2", "GET", DebugResponse(401, "Forbidden", null, null, null))))

        bridge.apply()

        verify { debuggerInterface.updateDelays(DebuggerDelays(123, 456, 789)) }
        verify { debuggerInterface.updateBlacklist(listOf("reg1")) }
        verify { debuggerInterface.updateDefaultResponses(listOf(activeDefaultResponse)) }
    }
}