package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser
import java.net.URLDecoder

/**
 * @author Nicola Verbeeck
 *
 * Parses the body as a x-form-urlencoded blob and splits it into a map of string keys to a list of values (to support duplicates)
 */
class URLEncodedBodyParser : BodyParser<Map<String, List<String>>> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): Map<String, List<String>>? {
        if (bytes.isEmpty())
            return emptyMap()

        val map: MutableMap<String, MutableList<String>> = mutableMapOf()

        String(bytes, BodyParser.makeCharset(bodyFormat)).split('&').forEach {
            val parts = it.split('=')
            val key = URLDecoder.decode(parts[0], "UTF-8")
            val value = URLDecoder.decode(parts[1], "UTF-8")
            map.getOrPut(key, { mutableListOf() }) += value
        }
        return map
    }

}