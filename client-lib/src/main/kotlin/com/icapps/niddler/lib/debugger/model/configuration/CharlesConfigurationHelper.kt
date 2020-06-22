package com.icapps.niddler.lib.debugger.model.configuration

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

interface CharlesConfigurationHelper {

    fun writeDocument(output: OutputStream, rootElement: String, documentBuilder: (document: Document, rootNode: Element) -> Unit) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        document.xmlStandalone = true

        val root = document.createElement(rootElement)
        document.appendChild(document.createProcessingInstruction("charles", "serialisation-version='2.0' "))
        document.appendChild(root)

        documentBuilder(document, root)

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        }
        transformer.transform(DOMSource(document), StreamResult(output))
    }

}