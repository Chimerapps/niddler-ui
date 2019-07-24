package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.logger
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Nicola Verbeeck
 *
 * Parses the body as a DOM document
 */
class XmlBodyParser : BodyParser<Document> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): Document? {
        if (bytes.isEmpty())
            return null

        return try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(bytes))
        } catch (e: Throwable) {
            logger<XmlBodyParser>().debug("Failed to parse xml: ", e)
            null
        }
    }

}