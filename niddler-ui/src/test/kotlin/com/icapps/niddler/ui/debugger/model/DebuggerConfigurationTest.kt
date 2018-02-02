package com.icapps.niddler.ui.debugger.model

import io.mockk.*
import org.junit.Before
import org.junit.Test


/**
 * @author nicolaverbeeck
 */
internal class DebuggerConfigurationTest {

    private lateinit var mockedService: DebuggerService
    private lateinit var debuggerInterface: DebuggerInterface

    @Before
    fun setUp() {
        mockedService = mockk()
        debuggerInterface = DebuggerConfiguration(mockedService)
    }

    @Test
    fun updateBlacklistAdd() {
        every { mockedService.addBlacklistItem(any()) } just Runs
        debuggerInterface.updateBlacklist(arrayListOf("test", "test2"))
        verify { mockedService.addBlacklistItem("test") }
        verify { mockedService.addBlacklistItem("test2") }
    }

    @Test
    fun updateBlacklistRemove() {

    }

    @Test
    fun updateDefaultResponses() {
    }

    @Test
    fun mute() {
        every { mockedService.setAllActionsMuted(any()) } just Runs
        debuggerInterface.mute()
        verify { mockedService.setAllActionsMuted(true) }
    }

    @Test
    fun unmute() {
        every { mockedService.setAllActionsMuted(any()) } just Runs
        debuggerInterface.unmute()
        verify { mockedService.setAllActionsMuted(false) }
    }
}