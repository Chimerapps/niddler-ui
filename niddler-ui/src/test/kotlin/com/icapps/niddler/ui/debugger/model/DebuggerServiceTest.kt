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
        val id = service.addDefaultResponse("299-2991-1-331", DebugResponse(200, "OK", mapOf("X-Client-Id" to "23019-10"),
                "ew0KCSJ0b2tlbiI6ICIxMjM4NzY3ODgxODgyOSINCn0=", "application/json"))
        verify { mockingedConnection.sendMessage("{\"controlType\":\"addDefaultResponse\",\"payload\":{\"regex\":\"299-2991-1-331\",\"id\":\"$id\",\"code\":200,\"message\":\"OK\",\"headers\":{\"X-Client-Id\":\"23019-10\"},\"encodedBody\":\"ew0KCSJ0b2tlbiI6ICIxMjM4NzY3ODgxODgyOSINCn0=\",\"bodyMimeType\":\"application/json\"},\"type\":\"controlDebug\"}") }

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
        service.respondTo("299-2991-1-331", DebugResponse(200, "OK", null, null, null))
        verify { mockingedConnection.sendMessage("{\"controlType\":\"debugReply\",\"payload\":{\"messageId\":\"299-2991-1-331\",\"code\":200,\"message\":\"OK\"},\"type\":\"controlDebug\"}") }
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
        service.updateDelays(DebuggerDelays(123, 456, 789))
        verify { mockingedConnection.sendMessage("{\"controlType\":\"updateDelays\",\"payload\":{\"preBlacklist\":123,\"postBlacklist\":456,\"timePerCall\":789},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun setAllActionsMuted() {
        service.setAllActionsMuted(true)
        verify { mockingedConnection.sendMessage("{\"controlType\":\"muteActions\",\"type\":\"controlDebug\"}") }

        service.setAllActionsMuted(false)
        verify { mockingedConnection.sendMessage("{\"controlType\":\"unmuteActions\",\"type\":\"controlDebug\"}") }
    }

    @Test
    fun addDefaultRequestOverride() {
        val id = service.addDefaultRequestOverride("299-2991-1-331", DebugRequest("https://www.google.com", "GET", mapOf("X-Client-Id" to "23019-10"),
                "ew0KCSJ0b2tlbiI6ICIxMjM4NzY3ODgxODgyOSINCn0=", "application/json"))
        verify { mockingedConnection.sendMessage("{\"controlType\":\"addDefaultRequestOverride\",\"payload\":{\"regex\":\"299-2991-1-331\",\"id\":\"$id\",\"url\":\"https://www.google.com\",\"method\":\"GET\",\"headers\":{\"X-Client-Id\":\"23019-10\"},\"encodedBody\":\"ew0KCSJ0b2tlbiI6ICIxMjM4NzY3ODgxODgyOSINCn0=\",\"bodyMimeType\":\"application/json\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun addRequestOverride() {
        val id = service.addRequestOverride("299-2991-1-331")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"addRequestOverride\",\"payload\":{\"regex\":\"299-2991-1-331\",\"id\":\"$id\"},\"type\":\"controlDebug\"}") }
    }

    @Test
    fun removeRequestOverrideMethod() {
        service.removeRequestOverrideMethod("ww-ff-qq")
        verify { mockingedConnection.sendMessage("{\"controlType\":\"removeRequestOverride\",\"payload\":{\"id\":\"ww-ff-qq\"},\"type\":\"controlDebug\"}") }
    }

}