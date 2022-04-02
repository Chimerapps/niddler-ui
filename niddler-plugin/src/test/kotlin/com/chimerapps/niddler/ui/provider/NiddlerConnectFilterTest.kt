package com.chimerapps.niddler.ui.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * @author Nicola Verbeeck
 */
class NiddlerConnectFilterTest {

    @Test
    fun testFindServerDoubleExtra() {
        val foundInfo = NiddlerConnectFilter.findServerStart("I/Niddler: Niddler Server running on 36829 [265dee][waitingForDebugger=false][paused]")
        assertEquals(36829, foundInfo?.port)
        assertEquals("265dee", foundInfo?.tag)
        assertEquals("false", foundInfo?.extras?.get("waitingForDebugger"))
        assertEquals("", foundInfo?.extras?.get("paused"))
        assertNull(foundInfo?.extras?.get("pausedNotThere"))
    }

    @Test
    fun testFindServerSingleExtra() {
        val foundInfo = NiddlerConnectFilter.findServerStart("I/Niddler: Niddler Server running on 36829 [265dee][waitingForDebugger=false]")
        assertEquals(36829, foundInfo?.port)
        assertEquals("265dee", foundInfo?.tag)
        assertEquals("false", foundInfo?.extras?.get("waitingForDebugger"))
        assertNull(foundInfo?.extras?.get("paused"))
        assertNull(foundInfo?.extras?.get("pausedNotThere"))
    }

    @Test
    fun testFindServerNoExtra() {
        val foundInfo = NiddlerConnectFilter.findServerStart("I/Niddler: Niddler Server running on 36829 [265dee]")
        assertEquals(36829, foundInfo?.port)
        assertEquals("265dee", foundInfo?.tag)
        assertNull(foundInfo?.extras?.get("waitingForDebugger"))
        assertNull(foundInfo?.extras?.get("paused"))
        assertNull(foundInfo?.extras?.get("pausedNotThere"))
    }

    @Test
    fun testFindServerNoExtraExtraChars() {
        val foundInfo = NiddlerConnectFilter.findServerStart("I/Niddler: Niddler Server running on 36829 [265dee]  weq")
        assertEquals(36829, foundInfo?.port)
        assertEquals("265dee", foundInfo?.tag)
        assertNull(foundInfo?.extras?.get("waitingForDebugger"))
        assertNull(foundInfo?.extras?.get("paused"))
        assertNull(foundInfo?.extras?.get("pausedNotThere"))
    }

    @Test
    fun testFindServerDoubleExtraExtraChars() {
        val foundInfo = NiddlerConnectFilter.findServerStart("I/Niddler: Niddler Server running on 36829 [265dee][waitingForDebugger=false][paused]qeq0d  12")
        assertEquals(36829, foundInfo?.port)
        assertEquals("265dee", foundInfo?.tag)
        assertEquals("false", foundInfo?.extras?.get("waitingForDebugger"))
        assertEquals("", foundInfo?.extras?.get("paused"))
        assertNull(foundInfo?.extras?.get("pausedNotThere"))
    }
}