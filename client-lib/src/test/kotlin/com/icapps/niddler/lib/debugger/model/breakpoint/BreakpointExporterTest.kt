package com.icapps.niddler.lib.debugger.model.breakpoint

import org.custommonkey.xmlunit.XMLUnit
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class BreakpointExporterTest {

    private lateinit var data: List<Breakpoint>

    @Before
    fun setup() {
        data = javaClass.getResourceAsStream("/breakpoints.xml").use { BreakpointImporter().import(it) }

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
    }

    @Test
    fun exportMatchesImported() {
        val out = ByteArrayOutputStream()
        BreakpointExporter().export(data, out)
        out.flush()

        val diff = javaClass.getResourceAsStream("/breakpoints.xml").use { expect ->
            XMLUnit.compareXML(expect.reader().readText(), out.toString(Charsets.UTF_8.name()))
        }
        diff.identical()
        assertTrue(diff.identical())
    }
}