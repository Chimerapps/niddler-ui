package com.icapps.niddler.ui.form.detail.body

import com.google.gson.GsonBuilder
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import javax.swing.text.Document

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerJsonDataPanel(message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(true, true, message) {

    init {
        initUI()
    }

    override fun createStructuredView() {
        this.structuredView = NiddlerJsonTree(message)
    }

    override fun createPrettyPrintedView(doc: Document) {
        doc.remove(0, doc.length)
        doc.insertString(0, GsonBuilder().setPrettyPrinting().create().toJson(message.bodyData), null)
    }

}