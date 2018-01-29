package com.icapps.niddler.ui.debugger.model

import com.icapps.niddler.ui.NiddlerDebuggerConnection
import io.mockk.*
import org.junit.Before
import org.junit.Test


/**
 * @author nicolaverbeeck
 */
internal class DebuggerServiceTest {

    private lateinit var mockingedConnection: NiddlerDebuggerConnection
    private lateinit var service: DebuggerService

    @Before
    fun setUp() {
        mockingedConnection = mockk()
        service = DebuggerService(mockingedConnection)
        every { mockingedConnection.sendMessage(any()) } just Runs
    }

    @Test
    fun addBlacklistItem() {
        service.addBlacklistItem(".*\\.*mdp")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"addBlacklist\",\"payload\":{\"regex\":\".*\\\\.*mdp\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun removeBlacklistItem() {
        service.removeBlacklistItem(".*\\.*mdp")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"removeBlacklist\",\"payload\":{\"regex\":\".*\\\\.*mdp\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun addDefaultResponse() {
        TODO("Implement test")
    }

    @Test
    fun addRequestIntercept() {
        val id = service.addRequestIntercept(".*\\.*mdp")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"addRequest\",\"payload\":{\"regex\":\".*\\\\.*mdp\",\"id\":\"$id\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun addResponseIntercept() {
        val id = service.addResponseIntercept(".*\\.*mdp")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"addResponse\",\"payload\":{\"regex\":\".*\\\\.*mdp\",\"id\":\"$id\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun respondTo() {
        TODO("Implement test")
    }

    @Test
    fun muteAction() {
        service.muteAction("ww-ff-qq")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"deactivateAction\",\"payload\":{\"id\":\"ww-ff-qq\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun unmuteAction() {
        service.unmuteAction("ww-ff-qq")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"activateAction\",\"payload\":{\"id\":\"ww-ff-qq\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun removeRequestAction() {
        service.removeRequestAction("ww-ff-qq")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"removeRequest\",\"payload\":{\"id\":\"ww-ff-qq\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun removeResponseAction() {
        service.removeResponseAction("ww-ff-qq")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"removeResponse\",\"payload\":{\"id\":\"ww-ff-qq\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun updateDelays() {
        service.updateDelays(DebuggerDelays(123, 456))
        verify { mockingedConnection.sendMessage("{\"controlType\":\"updateDelays\",\"payload\":{\"preBlacklist\":123,\"postBlacklist\":456},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun setAllActionsMuted() {
        service.setAllActionsMuted(true)
        verify { mockingedConnection.sendMessage("{\"controlType\":\"muteActions\",\"type\":\"controlDebug\"}") }

        service.setAllActionsMuted(false)
        verify { mockingedConnection.sendMessage("{\"controlType\":\"unmuteActions\",\"type\":\"controlDebug\"}") }
    }
}