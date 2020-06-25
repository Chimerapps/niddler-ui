package com.icapps.niddler.lib.debugger.model.configuration

import com.icapps.niddler.lib.debugger.model.rewrite.childWithTag
import com.icapps.niddler.lib.debugger.model.rewrite.createTextNode
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.InputStream
import java.io.OutputStream

interface BaseDebuggerConfiguration {
    val active: Boolean
    val name: String
    val locations: List<DebuggerLocationMatch>
    val id: String

    fun matchesUrl(url: String): Boolean {
        return locations.any { it.enabled && Regex(it.location.asRegex()).matches(url) }
    }
}

interface ConfigurationImporter<T : BaseDebuggerConfiguration> {

    fun import(stream: InputStream): List<T>

}

interface ConfigurationExporter<T : BaseDebuggerConfiguration> {

    fun export(configurations: Collection<T>, output: OutputStream)

}

interface DebuggerConfigurationFactory<T : BaseDebuggerConfiguration> {
    fun create(): T

    fun exporter(): ConfigurationExporter<T>

    fun importer(): ConfigurationImporter<T>
}

data class DebuggerLocationMatch(val enabled: Boolean,
                                 val location: DebugLocation)

data class DebugLocation(val protocol: String? = null,
                         val host: String? = null,
                         val port: String? = null,
                         val path: String? = null,
                         val query: String? = null) {

    fun asString(): String {
        var previousWasStar = false
        return buildString {
            if (protocol != null) {
                append(protocol).append("://")
            } else {
                previousWasStar = true
                append('*')
            }
            if (host != null) {
                if (previousWasStar)
                    append("://")
                append(host)
                previousWasStar = false
            } else if (!previousWasStar) {
                previousWasStar = true
                append('*')
            }
            if (port != null) {
                append(':').append(port)
                previousWasStar = false
            }
            if (path != null) {
                if (host == null && protocol == null)
                    append('*')
                if (!path.startsWith('/'))
                    append('/')
                append(path)
            }
            if (query != null) {
                if (!query.startsWith('?'))
                    append('?')
                append(query)
            }
        }
    }

    fun asRegex(): String {
        return buildString {
            if (protocol != null) {
                append(protocol).append("://")
            } else {
                append(".*://")
            }
            if (host != null) {
                append(host.replace("*", ".*").replace("?", ".?"))
            } else {
                append(".*")
            }
            if (port != null) {
                append(':').append(port.replace("*", "\\d*").replace("?", "\\d?"))
            } else {
                append("(:\\d+)?")
            }
            if (path != null) {
                if (!path.startsWith('/'))
                    append('/')
                append(path.replace("*", ".*").replace("?", ".?"))
            } else {
                append("(/[^?]*)?")
            }
            if (query != null) {
                if (!query.startsWith('?'))
                    append("\\?")
                append(query.replace("*", ".*").replace("?", ".?"))
            } else {
                append("(\\?[^#]*)?")
            }
            append("(#.*)?")
        }
    }

}

internal fun parseGenericLocationNode(node: Node): DebugLocation {
    val protocol = node.childWithTag("protocol")?.textContent?.trim()
    val host = node.childWithTag("host")?.textContent?.trim()
    val port = node.childWithTag("port")?.textContent?.trim()
    val path = node.childWithTag("path")?.textContent?.trim()
    val query = node.childWithTag("query")?.textContent?.trim()

    return DebugLocation(protocol = protocol,
            host = host,
            port = port,
            path = path,
            query = query)
}

internal fun createGenericLocationNode(location: DebugLocation, document: Document): Node {
    val root = document.createElement("location")

    location.protocol?.let { root.appendChild(document.createTextNode("protocol", it)) }
    location.host?.let { root.appendChild(document.createTextNode("host", it)) }
    location.port?.let { root.appendChild(document.createTextNode("port", it.toString())) }
    location.path?.let { root.appendChild(document.createTextNode("path", it)) }
    location.query?.let { root.appendChild(document.createTextNode("query", it)) }

    return root
}