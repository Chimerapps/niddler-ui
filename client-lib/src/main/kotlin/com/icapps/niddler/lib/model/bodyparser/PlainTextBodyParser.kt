package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser

/**
 * @author Nicola Verbeeck
 *
 * Parses the body as text. If no charset was specified by the format, assumes UTF-8
 */
class PlainTextBodyParser : BodyParser<String> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): String? {
        if (bytes.isEmpty())
            return ""
        return String(bytes, BodyParser.makeCharset(bodyFormat))
    }

}