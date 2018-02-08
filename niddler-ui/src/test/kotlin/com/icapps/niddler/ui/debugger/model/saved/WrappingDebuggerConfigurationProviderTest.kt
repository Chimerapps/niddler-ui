package com.icapps.niddler.ui.debugger.model.saved

import com.icapps.niddler.ui.debugger.model.DebugResponse
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.DefaultResponseAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.StringWriter

/**
 * @author nicolaverbeeck
 */
class WrappingDebuggerConfigurationProviderTest {

    @Test
    fun getDelayConfiguration() {
        val provider = WrappingDebuggerConfigurationProvider("{\"delays\": {\"enabled\": true,\"item\": {\"preBlacklist\": 123,\"postBlacklist\": 456,\"timePerCall\": 789}}}".reader())
        assertEquals(true, provider.delayConfiguration.enabled)
        assertEquals(123L, provider.delayConfiguration.item.preBlacklist)
        assertEquals(456L, provider.delayConfiguration.item.postBlacklist)
        assertEquals(789L, provider.delayConfiguration.item.timePerCall)
    }

    @Test
    fun getDelayConfigurationDefault() {
        val provider = WrappingDebuggerConfigurationProvider("".reader())
        assertEquals(false, provider.delayConfiguration.enabled)
        assertNull(provider.delayConfiguration.item.preBlacklist)
        assertNull(provider.delayConfiguration.item.postBlacklist)
        assertNull(provider.delayConfiguration.item.timePerCall)
    }

    @Test
    fun setDelayConfiguration() {
        val provider = WrappingDebuggerConfigurationProvider("".reader())
        provider.delayConfiguration = DisableableItem(true, DebuggerDelays(123L, 456L, 789L))
        assertEquals(true, provider.delayConfiguration.enabled)
        assertEquals(123L, provider.delayConfiguration.item.preBlacklist)
        assertEquals(456L, provider.delayConfiguration.item.postBlacklist)
        assertEquals(789L, provider.delayConfiguration.item.timePerCall)
    }

    @Test
    fun saveDelayConfigurationAfterSet() {
        val provider = WrappingDebuggerConfigurationProvider("".reader())
        provider.delayConfiguration = DisableableItem(true, DebuggerDelays(123L, 456L, 789L))

        val stringWriter = StringWriter()
        provider.save(stringWriter)
        assertEquals("{\"delays\":{\"enabled\":true,\"item\":{\"preBlacklist\":123,\"postBlacklist\":456,\"timePerCall\":789}}}", stringWriter.toString())
    }

    @Test
    fun getBlacklistConfiguration() {
        val provider = WrappingDebuggerConfigurationProvider("{\"blacklist\": [{\"enabled\": true,\"item\": \".*\\\\.json\"},{\"enabled\": false,\"item\": \".*\\\\.png\"}]}".reader())
        assertEquals(2, provider.blacklistConfiguration.size)

        assertEquals(true, provider.blacklistConfiguration[0].enabled)
        assertEquals(false, provider.blacklistConfiguration[1].enabled)

        assertEquals(".*\\.json", provider.blacklistConfiguration[0].item)
        assertEquals(".*\\.png", provider.blacklistConfiguration[1].item)
    }

    @Test
    fun getBlacklistConfigurationDefault() {
        val provider = WrappingDebuggerConfigurationProvider("weqweqcc][]".reader())
        assertEquals(0, provider.blacklistConfiguration.size)
    }

    @Test
    fun setBlacklistConfiguration() {
        val provider = WrappingDebuggerConfigurationProvider("".reader())
        assertEquals(0, provider.blacklistConfiguration.size)

        provider.blacklistConfiguration = listOf(DisableableItem(true, ".*\\.json"),
                DisableableItem(false, ".*\\.png"))

        val stringWriter = StringWriter()
        provider.save(stringWriter)
        assertEquals("{\"blacklist\":[{\"enabled\":true,\"item\":\".*\\\\.json\"},{\"enabled\":false,\"item\":\".*\\\\.png\"}]}", stringWriter.toString())
    }

    @Test
    fun getDefaultResponses() {
        val provider = WrappingDebuggerConfigurationProvider("{\"defaultResponses\":[{\"enabled\":true,\"item\":{\"regex\":\"reg1\",\"response\":{\"code\":200,\"message\":\"OK\"}}},{\"enabled\":false,\"item\":{\"regex\":\"reg2\",\"method\":\"GET\",\"response\":{\"code\":401,\"message\":\"Forbidden\"}}}]}".reader())
        assertEquals(2, provider.defaultResponses.size)

        assertEquals(true, provider.defaultResponses[0].enabled)
        assertNull(provider.defaultResponses[0].item.method)
        assertEquals("reg1", provider.defaultResponses[0].item.regex)
        assertEquals(200, provider.defaultResponses[0].item.response.code)
        assertEquals("OK", provider.defaultResponses[0].item.response.message)

        assertEquals(false, provider.defaultResponses[1].enabled)
        assertEquals("GET", provider.defaultResponses[1].item.method)
        assertEquals("reg2", provider.defaultResponses[1].item.regex)
        assertEquals(401, provider.defaultResponses[1].item.response.code)
        assertEquals("Forbidden", provider.defaultResponses[1].item.response.message)
    }

    @Test
    fun getDefaultResponsesDefault() {
        val provider = WrappingDebuggerConfigurationProvider("weqweqcc][]".reader())
        assertEquals(0, provider.defaultResponses.size)
    }

    @Test
    fun setDefaultResponses() {
        val provider = WrappingDebuggerConfigurationProvider("".reader())
        assertEquals(0, provider.defaultResponses.size)

        provider.defaultResponses = listOf(DisableableItem(true, DefaultResponseAction(null, false, "reg1", null, DebugResponse(200, "OK", null, null, null))),
                DisableableItem(false, DefaultResponseAction(null, false, "reg2", "GET", DebugResponse(401, "Forbidden", null, null, null))))

        val stringWriter = StringWriter()
        provider.save(stringWriter)
        assertEquals("{\"defaultResponses\":[{\"enabled\":true,\"item\":{\"regex\":\"reg1\",\"response\":{\"code\":200,\"message\":\"OK\"}}},{\"enabled\":false,\"item\":{\"regex\":\"reg2\",\"method\":\"GET\",\"response\":{\"code\":401,\"message\":\"Forbidden\"}}}]}", stringWriter.toString())
    }

    @Test
    fun save() {
        val provider = WrappingDebuggerConfigurationProvider("".reader())

        provider.delayConfiguration = DisableableItem(true, DebuggerDelays(123L, 456L, 789L))
        provider.blacklistConfiguration = listOf(DisableableItem(true, ".*\\.json"),
                DisableableItem(false, ".*\\.png"))

        val stringWriter = StringWriter()
        provider.save(stringWriter)
        assertEquals("{\"delays\":{\"enabled\":true,\"item\":{\"preBlacklist\":123,\"postBlacklist\":456,\"timePerCall\":789}},\"blacklist\":[{\"enabled\":true,\"item\":\".*\\\\.json\"},{\"enabled\":false,\"item\":\".*\\\\.png\"}]}", stringWriter.toString())
    }
}