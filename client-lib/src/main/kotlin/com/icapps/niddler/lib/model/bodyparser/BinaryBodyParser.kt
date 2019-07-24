package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser

/**
 * @author Nicola Verbeeck
 *
 * Just returns the given data
 */
class BinaryBodyParser : BodyParser<ByteArray> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): ByteArray? {
        return bytes
    }

}