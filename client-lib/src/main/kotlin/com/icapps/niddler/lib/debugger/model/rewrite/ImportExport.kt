package com.icapps.niddler.lib.debugger.model.rewrite

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class RewriteExporter {

    fun export(sets: Collection<RewriteSet>, output: OutputStream) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        document.xmlStandalone = true

        val root = document.createElement("rewriteSet-array")
        document.appendChild(document.createProcessingInstruction("charles", "serialisation-version='2.0' "))
        document.appendChild(root)

        sets.forEach { root.appendChild(writeSet(it, document)) }

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        }
        transformer.transform(DOMSource(document), StreamResult(output))
    }

    private fun writeSet(rewriteSet: RewriteSet, document: Document): Element {
        val root = document.createElement("rewriteSet")

        root.appendChild(document.createTextNode("active", rewriteSet.active.toString()))
        root.appendChild(document.createTextNode("name", rewriteSet.name))

        root.appendChild(writeLocations(rewriteSet.locations, document))
        root.appendChild(writeRules(rewriteSet.rules, document))

        return root
    }

    private fun writeRules(rules: List<RewriteRule>, document: Document): Node {
        val root = document.createElement("rules")
        rules.forEach { root.appendChild(writeRule(it, document)) }
        return root
    }

    private fun writeLocations(locations: List<RewriteLocationMatch>, document: Document): Node {
        val root = document.createElement("hosts")
        val patternsRoot = document.createElement("locationPatterns")
        root.appendChild(patternsRoot)

        locations.forEach { locationMatch ->
            patternsRoot.appendChild(writeLocationMatch(locationMatch, document))
        }

        return root
    }

    private fun writeLocationMatch(locationMatch: RewriteLocationMatch, document: Document): Node {
        val root = document.createElement("locationMatch")
        root.appendChild(writeLocation(locationMatch.location, document))
        root.appendChild(document.createTextNode("enabled", locationMatch.enabled.toString()))
        return root
    }

    private fun writeLocation(location: RewriteLocation, document: Document): Node {
        val root = document.createElement("location")

        location.protocol?.let { root.appendChild(document.createTextNode("protocol", it)) }
        location.host?.let { root.appendChild(document.createTextNode("host", it)) }
        location.port?.let { root.appendChild(document.createTextNode("port", it.toString())) }
        location.path?.let { root.appendChild(document.createTextNode("path", it)) }
        location.query?.let { root.appendChild(document.createTextNode("query", it)) }

        return root
    }

    private fun writeRule(rule: RewriteRule, document: Document): Node {
        val root = document.createElement("rewriteRule")

        root.appendChild(document.createTextNode("active", rule.active.toString()))
        root.appendChild(document.createTextNode("ruleType", rule.ruleType.charlesCode.toString()))
        rule.matchHeader?.let { root.appendChild(document.createTextNode("matchHeader", it)) }
        root.appendChild(document.createTextNode("matchValue", rule.matchValue ?: ""))
        root.appendChild(document.createTextNode("matchHeaderRegex", rule.matchHeaderRegex.toString()))
        root.appendChild(document.createTextNode("matchValueRegex", rule.matchValueRegex.toString()))
        root.appendChild(document.createTextNode("matchRequest", rule.matchRequest.toString()))
        root.appendChild(document.createTextNode("matchResponse", rule.matchResponse.toString()))
        if (rule.ruleType == RewriteType.ADD_HEADER || rule.ruleType == RewriteType.MODIFY_HEADER || rule.ruleType == RewriteType.ADD_QUERY_PARAM || rule.ruleType == RewriteType.MODIFY_QUERY_PARAM) {
            root.appendChild(document.createTextNode("newHeader", rule.newHeader ?: ""))
            root.appendChild(document.createTextNode("newValue", rule.newValue ?: ""))
        } else if (rule.ruleType == RewriteType.HOST || rule.ruleType == RewriteType.PATH || rule.ruleType == RewriteType.URL || rule.ruleType == RewriteType.RESPONSE_STATUS || rule.ruleType == RewriteType.BODY) {
            root.appendChild(document.createTextNode("newValue", rule.newValue ?: ""))
        }
        root.appendChild(document.createTextNode("newHeaderRegex", rule.newHeaderRegex.toString()))
        root.appendChild(document.createTextNode("newValueRegex", rule.newValueRegex.toString()))
        root.appendChild(document.createTextNode("matchWholeValue", rule.matchWholeValue.toString()))
        root.appendChild(document.createTextNode("caseSensitive", rule.caseSensitive.toString()))
        root.appendChild(document.createTextNode("replaceType", rule.replaceType.charlesCode.toString()))

        return root
    }

}

class RewriteImporter {

    fun import(stream: InputStream): List<RewriteSet> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        val root = document.documentElement
        if (root.tagName != "rewriteSet-array")
            throw IOException("Not a charles rewriteSet-array document")

        return root.childNodes.mapNotNullNodesWithName("rewriteSet", ::parseRewriteSet)
    }

    private fun parseRewriteSet(rewriteSet: Node): RewriteSet {
        val active = rewriteSet.childWithTag("active")?.textContent?.toBoolean() ?: false
        val name = rewriteSet.childWithTag("name")?.textContent ?: "<unknown>"
        val hosts = rewriteSet.childWithTag("hosts")
        val rules = rewriteSet.childWithTag("rules")

        val locations = hosts?.childNodes?.mapNotNullNodesWithName("locationPatterns", ::parseHostLocationPattern)?.flatten()
        val rewriteRules = rules?.childNodes?.mapNotNullNodesWithName("rewriteRule", ::parseRewriteRule)

        return RewriteSet(active = active,
                name = name,
                rules = rewriteRules.orEmpty(),
                locations = locations.orEmpty(),
                id = UUID.randomUUID().toString()
        )
    }

    private fun parseRewriteRule(rewriteRule: Node): RewriteRule? {
        val active = rewriteRule.childWithTag("active")?.textContent?.toBoolean() ?: false
        val ruleTypeInt = rewriteRule.childWithTag("ruleType")?.textContent?.toInt() ?: -1
        val matchHeaderRegex = rewriteRule.childWithTag("matchHeaderRegex")?.textContent?.toBoolean() ?: false
        val matchValueRegex = rewriteRule.childWithTag("matchValueRegex")?.textContent?.toBoolean() ?: false
        val matchRequest = rewriteRule.childWithTag("matchRequest")?.textContent?.toBoolean() ?: false
        val matchResponse = rewriteRule.childWithTag("matchResponse")?.textContent?.toBoolean() ?: false
        val newHeaderRegex = rewriteRule.childWithTag("newHeaderRegex")?.textContent?.toBoolean() ?: false
        val newValueRegex = rewriteRule.childWithTag("newValueRegex")?.textContent?.toBoolean() ?: false
        val matchWholeValue = rewriteRule.childWithTag("matchWholeValue")?.textContent?.toBoolean() ?: false
        val caseSensitive = rewriteRule.childWithTag("caseSensitive")?.textContent?.toBoolean() ?: false
        val replaceTypeInt = rewriteRule.childWithTag("replaceType")?.textContent?.toInt() ?: -1
        val matchHeader = rewriteRule.childWithTag("matchHeader")?.textContent?.emptyIsNull()
        val matchValue = rewriteRule.childWithTag("matchValue")?.textContent?.emptyIsNull()
        val newHeader = rewriteRule.childWithTag("newHeader")?.textContent
        val newValue = rewriteRule.childWithTag("newValue")?.textContent

        val type = RewriteType.fromCharlesCode(ruleTypeInt) ?: return null
        val replaceType = ReplaceType.fromCharlesCode(replaceTypeInt) ?: return null

        return RewriteRule(
                active = active,
                ruleType = type,
                matchHeaderRegex = matchHeaderRegex,
                matchValueRegex = matchValueRegex,
                matchRequest = matchRequest,
                matchResponse = matchResponse || type == RewriteType.RESPONSE_STATUS,
                newHeaderRegex = newHeaderRegex,
                newValueRegex = newValueRegex,
                matchWholeValue = matchWholeValue,
                caseSensitive = caseSensitive,
                replaceType = replaceType,
                matchHeader = matchHeader,
                matchValue = matchValue,
                newHeader = newHeader,
                newValue = newValue
        )
    }

    private fun parseHostLocationPattern(locationPatternNode: Node): List<RewriteLocationMatch> {
        return locationPatternNode.childNodes.mapNotNullNodesWithName("locationMatch", ::parseLocationMatch)
    }

    private fun parseLocationMatch(locationMatchNode: Node): RewriteLocationMatch? {
        val enabled = locationMatchNode.childWithTag("enabled")?.textContent?.toBoolean() ?: false
        val locationNode = locationMatchNode.childWithTag("location") ?: return null

        val protocol = locationNode.childWithTag("protocol")?.textContent?.trim()
        val host = locationNode.childWithTag("host")?.textContent?.trim()
        val port = locationNode.childWithTag("port")?.textContent?.trim()
        val path = locationNode.childWithTag("path")?.textContent?.trim()
        val query = locationNode.childWithTag("query")?.textContent?.trim()

        return RewriteLocationMatch(enabled = enabled,
                location = RewriteLocation(protocol = protocol,
                        host = host,
                        port = port,
                        path = path,
                        query = query))
    }

}

private fun String?.emptyIsNull(): String? = if (this.isNullOrEmpty()) null else this

private fun Node.childWithTag(name: String): Node? {
    val children = childNodes
    for (i in 0..children.length) {
        val child = children.item(i) ?: continue
        if (child.nodeName == name)
            return child
    }
    return null
}

private fun <T> NodeList.mapNotNullNodesWithName(name: String, map: (Node) -> T?): List<T> {
    val list = mutableListOf<T>()
    for (i in 0..length) {
        val hostChild = item(i) ?: continue
        if (hostChild.nodeName != name) continue

        map(hostChild)?.let(list::add)
    }
    return list
}

private fun Document.createTextNode(name: String, value: String): Element {
    val item = createElement(name)
    item.appendChild(createTextNode(value))
    return item
}