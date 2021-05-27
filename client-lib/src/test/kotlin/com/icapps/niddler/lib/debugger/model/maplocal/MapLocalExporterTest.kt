package com.icapps.niddler.lib.debugger.model.maplocal

import org.custommonkey.xmlunit.XMLUnit
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class MapLocalExporterTest {

    private lateinit var data: MapLocalConfiguration

    @Before
    fun setup() {
        data = javaClass.getResourceAsStream("/mapLocal.xml")!!.use { MapLocalImporter().import(it) }

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
    }

    @Test
    fun exportMatchesImported() {
        val out = ByteArrayOutputStream()
        MapLocalExporter().export(data, { file -> file }, out)
        out.flush()

        val diff = javaClass.getResourceAsStream("/mapLocal.xml")!!.use { expect ->
            XMLUnit.compareXML(expect.reader().readText(), out.toString(Charsets.UTF_8.name()))
        }
        assertTrue(diff.identical())
    }

    @Test
    fun exportMatchesImportedWithResolver() {
        val out = ByteArrayOutputStream()

        val copied = data.copy(mappings = listOf(data.mappings[0].copy(destination = "%projectDir%/overridden.json")))

        MapLocalExporter().export(copied, { file -> file.replace("%projectDir%", "/Users/example") }, out)
        out.flush()

        val diff = javaClass.getResourceAsStream("/mapLocal.xml")!!.use { expect ->
            XMLUnit.compareXML(expect.reader().readText(), out.toString(Charsets.UTF_8.name()))
        }
        assertTrue(diff.identical())
    }
}