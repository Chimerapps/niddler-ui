package com.icapps.niddler.lib.export.har

import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * @author Nicola Verbeeck
 * @date 09/11/2017.
 */
class StreamingHarWriter(target: OutputStream, creator: Creator, version: String = "1.2", comment: String? = null) {

    private val json = JsonWriter(OutputStreamWriter(target, Charsets.UTF_8))
    private val gson = Gson()

    init {
        json.beginObject()
        json.name("log")
        json.beginObject()
        json.name("version").value(version)
        json.name("creator")
        gson.toJson(creator, Creator::class.java, json)
        if (comment != null)
            json.name("comment").value(comment)

        json.name("entries")
        json.beginArray()
    }

    fun addEntry(entry: Entry) {
        gson.toJson(entry, Entry::class.java, json)
    }

    fun close() {
        json.endArray()
        json.endObject()
        json.endObject()

        json.close()
    }

}