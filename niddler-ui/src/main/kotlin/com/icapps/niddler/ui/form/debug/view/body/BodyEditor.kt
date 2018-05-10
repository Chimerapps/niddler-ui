package com.icapps.niddler.ui.form.debug.view.body

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.icapps.niddler.lib.model.classifier.BodyFormatType
import com.icapps.niddler.lib.model.classifier.ConcreteBody
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.debug.view.DebugMessageEntry
import com.icapps.niddler.ui.form.detail.body.NiddlerJsonEditableTree
import java.awt.BorderLayout
import java.awt.Font
import java.io.StringWriter
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
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

    private var currentEntry: DebugMessageEntry? = null

    private var currentType: BodyFormatType? = null
    private var currentBodyData: Any? = null
    private var currentDisplayTypes: List<DisplayType>? = null

    fun initWith(entry: DebugMessageEntry) {
        if (entry === currentEntry)
            return

        currentEntry = entry
        if (entry.response == null) {
            currentDisplayTypes = null
            removeAll()
        } else {
            val body = entry.modifiedBody
            if (body != null)
                initWithData(body.type, body.data)
            else
                initWithData(entry.response.bodyFormat.type, entry.response.bodyData)
        }
    }

    fun saveBody(): ConcreteBody? {
        val entry = currentEntry?.response ?: return null
        val displayTypes = currentDisplayTypes ?: return null
        when (currentType) {
            BodyFormatType.FORMAT_JSON -> {
                return ConcreteBody(currentType!!, entry.bodyFormat.subtype, (displayTypes[0].component as NiddlerJsonEditableTree).getEditedJson())
            }
        }
        return null
    }

    private fun initWithData(bodyFormat: BodyFormatType, bodyData: Any?) {
        currentType = bodyFormat
        currentBodyData = bodyData
        val displayTypes = mutableListOf<DisplayType>()
        when (bodyFormat) {
            BodyFormatType.FORMAT_JSON -> {
                displayTypes += DisplayType("Structured", NiddlerJsonEditableTree(bodyData as JsonElement))
                displayTypes += DisplayType("Pretty", JTextArea().apply {
                    font = Font("Monospaced", Font.PLAIN, 10)
                    document.insertString(0, GsonBuilder().setPrettyPrinting().create().toJson(bodyData), null)
                })
            }
            BodyFormatType.FORMAT_XML -> {
                displayTypes += DisplayType("Pretty", JTextArea().apply {
                    font = Font("Monospaced", Font.PLAIN, 10)
                    createPrettyPrintedView(document, bodyData)
                })
            }
            BodyFormatType.FORMAT_PLAIN -> displayTypes += DisplayType("Raw", JTextArea().apply {
                font = Font("Monospaced", Font.PLAIN, 10)
                document.insertString(0, bodyData?.toString(), null)
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
        currentDisplayTypes = displayTypes
        removeAll()
        if (displayTypes.isEmpty())
            return

        val tabList = componentsFactory.createTabComponent()

        displayTypes.forEach {
            tabList.addTab(it.name, JScrollPane(it.component))
        }

        add(tabList.asComponent, BorderLayout.CENTER)
    }

    private fun createPrettyPrintedView(doc: Document, bodyData: Any?) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        doc.remove(0, doc.length)

        if (bodyData is org.w3c.dom.Document) {
            val writer = StringWriter()
            transformer.transform(DOMSource(bodyData), StreamResult(writer))
            doc.insertString(0, writer.toString(), null)
        }
    }

    fun clear() {
        removeAll()
        currentDisplayTypes = null
    }

}

private data class DisplayType(val name: String, val component: JComponent)

