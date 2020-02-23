package com.icapps.niddler.lib.debugger.model.rewrite

import junit.framework.Assert.assertTrue
import org.custommonkey.xmlunit.XMLUnit
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class RewriteExporterTest {

    private lateinit var data: List<RewriteSet>

    @Before
    fun setup() {
        data = javaClass.getResourceAsStream("/rewrite.xml").use { RewriteImporter().import(it) }

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
    }

    @Test
    fun exportMatchesImported() {
        val out = ByteArrayOutputStream()
        RewriteExporter().export(data, out)
        out.flush()

        val diff = javaClass.getResourceAsStream("/rewrite.xml").use { expect ->
            XMLUnit.compareXML(expect.reader().readText(), out.toString(Charsets.UTF_8.name()))
        }
        assertTrue(diff.identical())
    }
}