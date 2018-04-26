package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import java.io.StringWriter
import javax.swing.text.Document
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerXMLDataPanel(message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(true, true, message) {

    init {
        initUI()
    }

    override fun createStructuredView() {
        this.structuredView = NiddlerXMLEditableTree(message)
        //this.structuredView = NiddlerXMLTree(message)
    }

    override fun createPrettyPrintedView(doc: Document) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, message.bodyFormat.encoding ?: "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val writer = StringWriter()
        transformer.transform(DOMSource(message.bodyData as org.w3c.dom.Document?), StreamResult(writer))

        doc.remove(0, doc.length)
        doc.insertString(0, writer.toString(), null)
    }

}