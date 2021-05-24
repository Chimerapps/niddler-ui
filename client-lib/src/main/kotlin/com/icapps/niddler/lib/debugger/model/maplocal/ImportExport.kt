package com.icapps.niddler.lib.debugger.model.maplocal

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteExporter.Companion.writeDocument
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteExporter.Companion.writeLocation
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteImporter.Companion.parseRewriteLocation
import com.icapps.niddler.lib.debugger.model.rewrite.childWithTag
import com.icapps.niddler.lib.debugger.model.rewrite.createTextNode
import com.icapps.niddler.lib.debugger.model.rewrite.mapNotNullNodesWithName
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Nicola Verbeeck
 */
class MapLocalExporter {

    fun export(configuration: MapLocalConfiguration, resolver: FileResolver, output: OutputStream) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        document.xmlStandalone = true

        val root = document.createElement("mapLocal")
        document.appendChild(document.createProcessingInstruction("charles", "serialisation-version='2.0' "))
        document.appendChild(root)

        root.appendChild(document.createTextNode("toolEnabled", configuration.enabled.toString()))

        val mappings = document.createElement("mappings")
        configuration.mappings.forEach { mappings.appendChild(writeMapping(it, resolver, document)) }
        root.appendChild(mappings)

        writeDocument(document, output)
    }

    private fun writeMapping(localEntry: MapLocalEntry, resolver: FileResolver, document: Document): Node {
        val node = document.createElement("mapLocalMapping")

        node.appendChild(writeLocation(localEntry.location, document, "sourceLocation"))
        node.appendChild(document.createTextNode("dest", resolver.resolveFile(localEntry.destination)))
        node.appendChild(document.createTextNode("enabled", localEntry.enabled.toString()))
        node.appendChild(document.createTextNode("caseSensitive", localEntry.caseSensitive.toString()))

        return node
    }

}

class MapLocalImporter {

    fun import(stream: InputStream): MapLocalConfiguration {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        val root = document.documentElement
        if (root.tagName != "mapLocal")
            throw IOException("Not a charles mapLocal document")

        val enabled = (root.childWithTag("toolEnabled")?.textContent?.toBoolean() ?: false)

        val nodes = root.childWithTag("mappings")?.childNodes?.mapNotNullNodesWithName("mapLocalMapping", ::parseMapping)

        return MapLocalConfiguration(enabled = enabled, mappings = nodes ?: emptyList())
    }

    private fun parseMapping(localMappingNode: Node): MapLocalEntry {
        return MapLocalEntry(
            location = parseRewriteLocation(localMappingNode.childWithTag("sourceLocation")!!),
            destination = localMappingNode.childWithTag("dest")!!.textContent,
            enabled = localMappingNode.childWithTag("enabled")?.textContent?.toBoolean() ?: false,
            caseSensitive = localMappingNode.childWithTag("caseSensitive")?.textContent?.toBoolean() ?: false,
            id = UUID.randomUUID().toString(),
        )
    }
}