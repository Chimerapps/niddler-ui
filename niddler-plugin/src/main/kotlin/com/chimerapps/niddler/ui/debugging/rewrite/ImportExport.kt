package com.chimerapps.niddler.ui.debugging.rewrite

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

class RewriteExporter {


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
                locations = locations.orEmpty()
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

        val type = RewriteType.fromCharlesCode(ruleTypeInt) ?: return null
        val replaceType = ReplaceType.fromCharlesCode(replaceTypeInt) ?: return null

        return RewriteRule(
                active = active,
                ruleType = type,
                matchHeaderRegex = matchHeaderRegex,
                matchValueRegex = matchValueRegex,
                matchRequest = matchRequest,
                matchResponse = matchResponse,
                newHeaderRegex = newHeaderRegex,
                newValueRegex = newValueRegex,
                matchWholeValue = matchWholeValue,
                caseSensitive = caseSensitive,
                replaceType = replaceType
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
        val port = locationNode.childWithTag("port")?.textContent?.toInt()
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

fun main() {
    FileInputStream("/Users/nicolaverbeeck/Desktop/testing123.xml").use {
        println(RewriteImporter().import(it))
    }
}