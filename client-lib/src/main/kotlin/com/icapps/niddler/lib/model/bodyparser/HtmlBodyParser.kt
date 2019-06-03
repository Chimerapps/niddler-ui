package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser
import org.w3c.dom.Document

/**
 * @author Nicola Verbeeck
 *
 * Parses the body as a HTML document represented by the DOM document. Note that this only accepts valid XML documents for now
 */
class HtmlBodyParser : BodyParser<Document> {

    private val delegate = XmlBodyParser()

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): Document? {
        return delegate.parse(bodyFormat, bytes)
    }


}