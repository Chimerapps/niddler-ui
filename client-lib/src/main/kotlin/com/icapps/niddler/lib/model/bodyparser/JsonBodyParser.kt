package com.icapps.niddler.lib.model.bodyparser

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser
import com.icapps.niddler.lib.utils.debug
import com.icapps.niddler.lib.utils.logger
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/**
 * @author Nicola Verbeeck
 *
 * Parses the body as a GSON json element
 */
class JsonBodyParser : BodyParser<JsonElement> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): JsonElement? {
        if (bytes.isEmpty())
            return null
        return try {
            JsonParser().parse(InputStreamReader(ByteArrayInputStream(bytes), BodyParser.makeCharset(bodyFormat)))
        } catch (e: Exception) {
            logger<JsonBodyParser>().debug("Failed to parse json: ", e)
            null
        }
    }

}