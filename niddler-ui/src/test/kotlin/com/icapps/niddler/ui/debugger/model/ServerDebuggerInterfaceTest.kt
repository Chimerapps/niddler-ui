package com.icapps.niddler.ui.debugger.model

import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.*


/**
 * @author nicolaverbeeck
 */
internal class ServerDebuggerInterfaceTest {

    private lateinit var mockedService: DebuggerService
    private lateinit var debuggerInterface: DebuggerInterface

    @Before
    fun setUp() {
        mockedService = mockk()
        debuggerInterface = ServerDebuggerInterface(mockedService)
    }

    @Test
    fun updateBlacklistAdd() {
        every { mockedService.addBlacklistItem(any()) } just Runs
        debuggerInterface.updateBlacklist(arrayListOf("test", "test2"))
        verify { mockedService.addBlacklistItem("test") }
        verify { mockedService.addBlacklistItem("test2") }
    }

    @Test
    fun updateBlacklistRemoveAll() {
        every { mockedService.addBlacklistItem(any()) } just Runs
        every { mockedService.removeBlacklistItem(any()) } just Runs
        debuggerInterface.updateBlacklist(arrayListOf("test", "test2"))
        debuggerInterface.updateBlacklist(arrayListOf())
        verify { mockedService.removeBlacklistItem("test") }
        verify { mockedService.removeBlacklistItem("test2") }
    }

    @Test
    fun updateBlacklistRemoveAndAdd() {
        every { mockedService.addBlacklistItem(any()) } just Runs
        every { mockedService.removeBlacklistItem(any()) } just Runs

        debuggerInterface.updateBlacklist(arrayListOf("test", "test2"))
        verify(exactly = 1) { mockedService.addBlacklistItem("test") }
        verify(exactly = 1) { mockedService.addBlacklistItem("test2") }

        debuggerInterface.updateBlacklist(arrayListOf("test3", "test"))

        verify(exactly = 1) { mockedService.addBlacklistItem("test") }
        verify { mockedService.removeBlacklistItem("test2") }
        verify { mockedService.addBlacklistItem("test3") }
    }

    @Test
    fun updateDefaultResponses() {
        val uuids = mutableListOf<String>()

        every { mockedService.addDefaultResponse(".*\\.png", any(), any(), any()) } answers {
            val id = UUID.randomUUID().toString()
            uuids += id
            id
        }

        val actions = listOf(LocalRequestIntercept("1", true, ".*\\.png", "GET", null, DebugResponse(200, "OK", null, null, null)),
                LocalRequestIntercept("2", true, ".*\\.png", "POST", null, DebugResponse(404, "Not found", null, null, null)))

        debuggerInterface.updateDefaultResponses(actions)

        verify { mockedService.addDefaultResponse(actions[0].regex, actions[0].matchMethod, actions[0].debugResponse!!, actions[0].active) }
        verify { mockedService.addDefaultResponse(actions[1].regex, actions[1].matchMethod, actions[1].debugResponse!!, actions[1].active) }

        assertEquals(uuids[0], actions[0].id)
        assertEquals(uuids[1], actions[1].id)
    }

    @Test
    fun updateDefaultResponsesRemoveAll() {
        val uuids = mutableListOf<String>()

        every { mockedService.addDefaultResponse(".*\\.png", any(), any(), any()) } answers {
            val id = UUID.randomUUID().toString()
            uuids += id
            id
        }
        every { mockedService.removeRequestAction(any()) } just Runs

        val actions = listOf(LocalRequestIntercept("1", true, ".*\\.png", null, null, DebugResponse(200, "OK", null, null, null)),
                LocalRequestIntercept("2", true, ".*\\.png", "HEAD", null, DebugResponse(404, "Not found", null, null, null)))

        debuggerInterface.updateDefaultResponses(actions)

        assertEquals(uuids[0], actions[0].id)
        assertEquals(uuids[1], actions[1].id)

        debuggerInterface.updateDefaultResponses(emptyList())

        verify(exactly = 1) { mockedService.addDefaultResponse(actions[0].regex, actions[0].matchMethod, actions[0].debugResponse!!, actions[0].active) }
        verify(exactly = 1) { mockedService.addDefaultResponse(actions[1].regex, actions[1].matchMethod, actions[1].debugResponse!!, actions[1].active) }

        verify { mockedService.removeRequestAction(or(uuids[0], uuids[1])) }
    }

    @Test
    fun updateDefaultResponsesAddAndRemove() {
        val uuids = mutableListOf<String>()

        every { mockedService.addDefaultResponse(".*\\.png", any(), any(), any()) } answers {
            val id = UUID.randomUUID().toString()
            uuids += id
            id
        }
        every { mockedService.removeRequestAction(any()) } just Runs

        val actions = listOf(LocalRequestIntercept("1", true, ".*\\.png", "HEAD", null, DebugResponse(200, "OK", null, null, null)),
                LocalRequestIntercept("2", true, ".*\\.png", "GET", null, DebugResponse(404, "Not found", null, null, null)))

        debuggerInterface.updateDefaultResponses(actions)

        assertEquals(uuids[0], actions[0].id)
        assertEquals(uuids[1], actions[1].id)

        val secondActions = listOf(actions[1], LocalRequestIntercept("3", true, ".*\\.png", "POST", null, DebugResponse(404, "Not found", null, null, null)))
        debuggerInterface.updateDefaultResponses(secondActions)

        verify(exactly = 1) { mockedService.addDefaultResponse(actions[0].regex, actions[0].matchMethod, actions[0].debugResponse!!, actions[0].active) }
        verify(exactly = 1) { mockedService.addDefaultResponse(actions[1].regex, actions[1].matchMethod, actions[1].debugResponse!!, actions[1].active) }
        verify(exactly = 1) { mockedService.addDefaultResponse(secondActions[1].regex, secondActions[1].matchMethod, secondActions[1].debugResponse!!, secondActions[1].active) }

        verify(exactly = 1) { mockedService.removeRequestAction(uuids[0]) }
    }

    @Test
    fun updateDefaultResponsesActivateLater() {
        val uuids = mutableListOf<String>()

        every { mockedService.addDefaultResponse(".*\\.png", any(), any(), any()) } answers {
            val id = UUID.randomUUID().toString()
            uuids += id
            id
        }
        every { mockedService.muteAction(any()) } just Runs
        every { mockedService.unmuteAction(any()) } just Runs

        val actions = listOf(LocalRequestIntercept("1", true, ".*\\.png", "DELETE", null, DebugResponse(200, "OK", null, null, null)),
                LocalRequestIntercept("2", false, ".*\\.png", "subscribe", null, DebugResponse(404, "Not found", null, null, null)))

        debuggerInterface.updateDefaultResponses(actions)

        assertEquals(uuids[0], actions[0].id)
        assertEquals(uuids[1], actions[1].id)

        actions[0].active = false
        actions[1].active = true

        debuggerInterface.updateDefaultResponses(actions)

        verify(exactly = 1) { mockedService.addDefaultResponse(actions[0].regex, actions[0].matchMethod, actions[0].debugResponse!!, any()) }
        verify(exactly = 1) { mockedService.addDefaultResponse(actions[1].regex, actions[1].matchMethod, actions[1].debugResponse!!, any()) }

        verify(exactly = 1) { mockedService.muteAction(uuids[0]) }
        verify(exactly = 1) { mockedService.unmuteAction(uuids[1]) }
    }

    @Test
    fun mute() {
        every { mockedService.setAllActionsMuted(any()) } just Runs
        debuggerInterface.mute()
        verify(exactly = 1) { mockedService.setAllActionsMuted(true) }
    }

    @Test
    fun unmute() {
        every { mockedService.setAllActionsMuted(any()) } just Runs
        debuggerInterface.unmute()
        verify(exactly = 1) { mockedService.setAllActionsMuted(false) }
    }

    @Test
    fun updateDelaysDisable() {
        every { mockedService.updateDelays(any()) } just Runs
        debuggerInterface.updateDelays(DebuggerDelays(123, 456, 789))
        debuggerInterface.updateDelays(null)
        verify(exactly = 1) { mockedService.updateDelays(DebuggerDelays(null, null, null)) }
        assertNull(debuggerInterface.debugDelays())
    }

    @Test
    fun updateDelaysEnable() {
        every { mockedService.updateDelays(DebuggerDelays(123, 456, 789)) } just Runs
        debuggerInterface.updateDelays(DebuggerDelays(123, 456, 789))
        verify(exactly = 1) { mockedService.updateDelays(DebuggerDelays(123, 456, 789)) }
        assertEquals(DebuggerDelays(123, 456, 789), debuggerInterface.debugDelays())
    }

    @Test
    fun activate() {
        every { mockedService.setActive(any()) } just Runs
        debuggerInterface.activate()
        verify(exactly = 1) { mockedService.setActive(true) }
    }

    @Test
    fun deactivate() {
        every { mockedService.setActive(any()) } just Runs
        debuggerInterface.deactivate()
        verify(exactly = 1) { mockedService.setActive(false) }
    }

    @Test
    fun connect() {
        every { mockedService.connect() } just Runs
        debuggerInterface.connect()
        verify(exactly = 1) { mockedService.connect() }
    }

    @Test
    fun disconnect() {
        every { mockedService.disconnect() } just Runs
        debuggerInterface.disconnect()
        verify(exactly = 1) { mockedService.disconnect() }
    }
}