package com.icapps.niddler.ui.form.debug.view.body

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.lib.model.classifier.BodyFormatType
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.detail.body.NiddlerJsonEditableTree
import java.awt.BorderLayout
import java.awt.Font
import java.io.StringWriter
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.text.Document
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * @author nicolaverbeeck
 */
class BodyEditor(private val componentsFactory: ComponentsFactory) : JPanel(BorderLayout()) {

    fun initWith(message: ParsedNiddlerMessage) {
        val displayTypes = mutableListOf<DisplayType>()
        when (message.bodyFormat.type) {
            BodyFormatType.FORMAT_JSON -> {
                displayTypes += DisplayType("Structured", NiddlerJsonEditableTree(message.bodyData as JsonElement))
                displayTypes += DisplayType("Pretty", JTextArea().apply {
                    font = Font("Monospaced", Font.PLAIN, 10)
                    document.insertString(0, GsonBuilder().setPrettyPrinting().create().toJson(message.bodyData), null)
                })
                displayTypes += DisplayType("Raw", JTextArea().apply {
                    font = Font("Monospaced", Font.PLAIN, 10)
                    document.insertString(0, message.getBodyAsString("utf-8"), null)
                })
            }
            BodyFormatType.FORMAT_XML -> {
                displayTypes += DisplayType("Pretty", JTextArea().apply {
                    font = Font("Monospaced", Font.PLAIN, 10)
                    createPrettyPrintedView(document, message)
                })
                displayTypes += DisplayType("Raw", JTextArea().apply {
                    font = Font("Monospaced", Font.PLAIN, 10)
                    document.insertString(0, message.getBodyAsString("utf-8"), null)
                })
            }
            BodyFormatType.FORMAT_PLAIN -> displayTypes += DisplayType("Raw", JTextArea().apply {
                font = Font("Monospaced", Font.PLAIN, 10)
                document.insertString(0, message.getBodyAsString("utf-8"), null)
            })
            BodyFormatType.FORMAT_IMAGE -> {
            }
            BodyFormatType.FORMAT_BINARY -> {
            }
            BodyFormatType.FORMAT_HTML -> {
            }
            BodyFormatType.FORMAT_EMPTY -> {
            }
            BodyFormatType.FORMAT_FORM_ENCODED -> {
            }
        }
        //TODO missing type
        buildFromDisplayTypes(displayTypes)
    }

    private fun buildFromDisplayTypes(displayTypes: List<DisplayType>) {
        removeAll()
        if (displayTypes.isEmpty())
            return
        val tabList = componentsFactory.createTabComponent()

        displayTypes.forEach {
            tabList.addTab(it.name, it.component)
        }

        add(tabList.asComponent, BorderLayout.CENTER)
    }

    private fun createPrettyPrintedView(doc: Document, message: ParsedNiddlerMessage) {
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

private data class DisplayType(val name: String, val component: JComponent)

