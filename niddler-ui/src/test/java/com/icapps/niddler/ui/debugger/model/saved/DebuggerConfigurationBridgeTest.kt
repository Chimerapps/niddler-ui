package com.icapps.niddler.ui.debugger.model.saved

import com.icapps.niddler.ui.debugger.model.DebuggerInterface
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

/**
 * @author nicolaverbeeck
 */
class DebuggerConfigurationBridgeTest {

    private lateinit var configurationProvider: DebuggerConfigurationProvider
    private lateinit var debuggerInterface: DebuggerInterface

    @Before
    fun setUp() {
        configurationProvider = mockk()
        debuggerInterface = mockk()
    }

    @Test
    fun apply() {

    }
}