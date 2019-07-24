package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser

/**
 * @author Nicola Verbeeck
 *
 * Parses the body as a HTML document represented by the string
 */
class HtmlBodyParser : BodyParser<String> {

    private val delegate = PlainTextBodyParser()

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): String? {
        return delegate.parse(bodyFormat, bytes)
    }


}