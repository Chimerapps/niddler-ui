package com.icapps.niddler.ui.debugger.model.saved

import com.icapps.niddler.ui.debugger.model.DebuggerDelays
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