package com.chimerapps.niddler.ui.debugging.rewrite

import org.w3c.dom.Node
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

class RewriteExporter {


}

class RewriteImporter {

    fun import(stream: InputStream) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        val root = document.documentElement
        if (root.tagName != "rewriteSet-array")
            throw IOException("Not a charles rewriteSet-array document")

        val rewriteSets = root.childNodes
        for (i in 0..rewriteSets.length) {
            val rewriteSet = root.childNodes.item(i) ?: continue
            if (rewriteSet.nodeName != "rewriteSet") continue
            parseRewriteSet(rewriteSet)
        }
    }

    private fun parseRewriteSet(rewriteSet: Node) {
        val active = rewriteSet.childWithTag("active")?.textContent?.toBoolean() ?: false
        val name = rewriteSet.childWithTag("name")?.textContent
        val hosts = rewriteSet.childWithTag("hosts")
        val rules = rewriteSet.childWithTag("rules")

        hosts?.let {
            val hostChildren = it.childNodes
            for (i in 0..hostChildren.length) {
                val hostChild = hostChildren.item(i) ?: continue
                if (hostChild.nodeName != "locationPatterns") continue

                parseHostLocationPattern(hostChild)
            }
        }
        rules?.let {
            val rulesChildren = rules.childNodes
            for (i in 0..rulesChildren.length) {
                val rewriteRule = rulesChildren.item(i) ?: continue
                if (rewriteRule.nodeName != "rewriteRule") continue

                parseRewriteRule(rewriteRule)
            }
        }

        println("$name. Active: $active")
    }

    private fun parseRewriteRule(rewriteRule: Node) {
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
        //TODO
    }

    private fun parseHostLocationPattern(hostChild: Node) {
        //TODO
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

fun main() {
    FileInputStream("/Users/nicolaverbeeck/Desktop/testing123.xml").use {
        RewriteImporter().import(it)
    }
}