package com.icapps.niddler.lib.debugger.model.breakpoint

import com.icapps.niddler.lib.debugger.model.configuration.ConfigurationExporter
import com.icapps.niddler.lib.debugger.model.configuration.ConfigurationImporter
import com.icapps.niddler.lib.debugger.model.configuration.DebuggerConfigurationFactory
import com.icapps.niddler.lib.debugger.model.configuration.DebuggerLocationMatch
import com.icapps.niddler.lib.debugger.model.configuration.createGenericLocationNode
import com.icapps.niddler.lib.debugger.model.configuration.parseGenericLocationNode
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
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object BreakpointDebuggerConfigurationFactory : DebuggerConfigurationFactory<Breakpoint> {
    override fun create(): Breakpoint = Breakpoint(active = true, name = "Unnamed", locations = emptyList(), method = null,
            request = false, response = false, id = UUID.randomUUID().toString())

    override fun exporter(): ConfigurationExporter<Breakpoint> = BreakpointExporter()

    override fun importer(): ConfigurationImporter<Breakpoint> = BreakpointImporter()
}

class BreakpointImporter : ConfigurationImporter<Breakpoint> {

    override fun import(stream: InputStream): List<Breakpoint> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        val root = document.documentElement
        if (root.tagName != "breakpoints")
            throw IOException("Not a charles breakpoints document")

        return root.childWithTag("breakpoints")?.let(::parseBreakpoints) ?: emptyList()
    }

    private fun parseBreakpoints(node: Node): List<Breakpoint> {
        return node.childNodes.mapNotNullNodesWithName("breakpoint", ::parseBreakpoint)
    }

    private fun parseBreakpoint(node: Node): Breakpoint? {
        val location = parseLocation(node) ?: return null
        val enabled = node.childWithTag("enabled")?.textContent?.toBoolean() ?: false
        val request = node.childWithTag("request")?.textContent?.toBoolean() ?: false
        val response = node.childWithTag("response")?.textContent?.toBoolean() ?: false
        val method = node.childWithTag("scheme")?.textContent?.trim()

        return Breakpoint(active = enabled, locations = listOf(location), response = response,
                request = request, name = location.location.asString(), id = UUID.randomUUID().toString(), method = method)
    }

    private fun parseLocation(parentNode: Node): DebuggerLocationMatch? {
        val locationNode = parentNode.childWithTag("location") ?: return null

        return DebuggerLocationMatch(enabled = true, location = parseGenericLocationNode(locationNode))
    }

}

class BreakpointExporter : ConfigurationExporter<Breakpoint> {

    override fun export(configurations: Collection<Breakpoint>, output: OutputStream) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        document.xmlStandalone = true

        val root = document.createElement("breakpoints")
        document.appendChild(document.createProcessingInstruction("charles", "serialisation-version='2.0' "))
        document.appendChild(root)

        root.appendChild(document.createTextNode("toolEnabled", "true"))

        val breakpointRoots = document.createElement("breakpoints")
        root.appendChild(breakpointRoots)

        configurations.forEach { breakpointRoots.appendChild(writeBreakpoint(it, document)) }

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        }
        transformer.transform(DOMSource(document), StreamResult(output))
    }

    private fun writeBreakpoint(breakpoint: Breakpoint, document: Document): Node {
        val root = document.createElement("breakpoint")

        root.appendChild(createGenericLocationNode(breakpoint.locations[0].location, document))
        root.appendChild(document.createTextNode("request", breakpoint.request.toString()))
        root.appendChild(document.createTextNode("response", breakpoint.response.toString()))
        root.appendChild(document.createTextNode("enabled", breakpoint.active.toString()))
        breakpoint.method?.let { root.appendChild(document.createTextNode("scheme", it)) }

        return root
    }

}