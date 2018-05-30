package com.icapps.niddler.lib.debugger.model.saved

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonWriter
import com.icapps.niddler.lib.debugger.model.DebuggerDelays
import com.icapps.niddler.lib.debugger.model.LocalRequestIntercept
import com.icapps.niddler.lib.debugger.model.LocalRequestOverride
import com.icapps.niddler.lib.debugger.model.LocalResponseIntercept
import com.icapps.niddler.lib.utils.createGsonListType
import java.io.File
import java.io.Reader
import java.io.Writer
import java.lang.reflect.Type
import kotlin.reflect.KProperty

/**
 * @author nicolaverbeeck
 */
class WrappingDebuggerConfiguration : DebuggerConfiguration {

    override var delayConfiguration: DisableableItem<DebuggerDelays> by jsonWrap("delays") {
        DisableableItem(false, DebuggerDelays(null, null, null))
    }
    override var blacklistConfiguration: List<DisableableItem<String>> by jsonWrapList("blacklist") {
        emptyList<DisableableItem<String>>()
    }
    override var requestIntercept: List<DisableableItem<LocalRequestIntercept>> by jsonWrapList("requestIntercepts") {
        emptyList<DisableableItem<LocalRequestIntercept>>()
    }
    override var requestOverride: List<DisableableItem<LocalRequestOverride>> by jsonWrapList("requestOverrides") {
        emptyList<DisableableItem<LocalRequestOverride>>()
    }
    override var responseIntercept: List<DisableableItem<LocalResponseIntercept>> by jsonWrapList("responseIntercepts") {
        emptyList<DisableableItem<LocalResponseIntercept>>()
    }

    private val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create()
    private val configurationTree: JsonObject

    constructor(stream: Reader) {
        configurationTree = try {
            JsonParser().parse(stream).asJsonObject
        } catch (e: Throwable) {
            JsonObject()
        }
    }

    constructor() {
        configurationTree = JsonObject()
    }

    constructor(source: DebuggerConfiguration) {
        configurationTree = JsonObject()
        delayConfiguration = source.delayConfiguration.copy()
        blacklistConfiguration = source.blacklistConfiguration.map { it.copy() }
        requestIntercept = source.requestIntercept.map { it.copy() }
        requestOverride = source.requestOverride.map { it.copy() }
        responseIntercept = source.responseIntercept.map { it.copy() }
    }

    fun save(target: File, pretty: Boolean) {
        target.writer(Charsets.UTF_8).use { save(it, pretty) }
    }

    fun save(writer: Writer, pretty: Boolean) {
        val jsonWriter = JsonWriter(writer)
        if (pretty)
            jsonWriter.apply {
                isHtmlSafe = false
                isLenient = false
                setIndent("  ")
            }
        gson.toJson(configurationTree, jsonWriter)
        jsonWriter.flush()
    }

    private fun getNode(name: String): JsonElement? {
        return configurationTree.get(name)
    }

    private fun setNode(name: String, value: Any) {
        configurationTree.add(name, gson.toJsonTree(value))
    }

    private fun <T> getNodeAs(name: String, type: Type): T? {
        val node = getNode(name) ?: return null
        return try {
            gson.fromJson<T>(node, type)
        } catch (e: Throwable) {
            null
        }
    }

    private inline fun <reified T : Any> jsonWrap(name: String,
                                                  noinline defaultCreator: () -> T): ExtractingDelegate<T> {
        return ExtractingDelegate(name, this, object : TypeToken<T>() {}.type, defaultCreator)
    }

    private inline fun <reified T : Any> jsonWrapList(name: String,
                                                      noinline defaultCreator: () -> List<T>): ExtractingDelegate<List<T>> {
        return ExtractingDelegate(name, this, createGsonListType<T>(), defaultCreator)
    }

    private class ExtractingDelegate<T : Any>(private val name: String,
                                              private val parent: WrappingDebuggerConfiguration,
                                              private val type: Type,
                                              private val defaultCreator: () -> T) {

        private val lock = Any()
        private var cachedField: T? = null

        operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            synchronized(lock) {
                cachedField?.let { return it }
                var value = parent.getNodeAs<T>(name, type)
                if (value == null) {
                    value = defaultCreator()
                    parent.setNode(name, value)
                }
                cachedField = value
                return value
            }
        }

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            synchronized(lock) {
                cachedField = value
                parent.setNode(name, value)
            }
        }

    }
}