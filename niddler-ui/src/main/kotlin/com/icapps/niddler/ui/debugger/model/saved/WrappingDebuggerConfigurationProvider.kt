package com.icapps.niddler.ui.debugger.model.saved

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonWriter
import com.icapps.niddler.ui.debugger.model.DebuggerDelays
import com.icapps.niddler.ui.debugger.model.DefaultResponseAction
import com.icapps.niddler.ui.util.createGsonListType
import java.io.File
import java.io.Reader
import java.io.Writer
import java.lang.reflect.Type
import kotlin.reflect.KProperty

/**
 * @author nicolaverbeeck
 */
class WrappingDebuggerConfigurationProvider : DebuggerConfigurationProvider {

    override var delayConfiguration: DisableableItem<DebuggerDelays> by jsonWrap("delays") {
        DisableableItem(false, DebuggerDelays(null, null, null))
    }
    override var blacklistConfiguration: List<DisableableItem<String>> by jsonWrapList("blacklist") {
        emptyList<DisableableItem<String>>()
    }
    override var defaultResponses: List<DisableableItem<DefaultResponseAction>> by jsonWrapList("defaultResponses") {
        emptyList<DisableableItem<DefaultResponseAction>>()
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

    constructor(file: File) {
        configurationTree = try {
            file.reader(Charsets.UTF_8).use {
                JsonParser().parse(it).asJsonObject
            }
        } catch (e: Throwable) {
            JsonObject()
        }
    }

    fun save(target: File) {
        target.writer(Charsets.UTF_8).use { save(it) }
    }

    fun save(writer: Writer) {
        gson.toJson(configurationTree, JsonWriter(writer))
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
                                              private val parent: WrappingDebuggerConfigurationProvider,
                                              private val type: Type,
                                              private val defaultCreator: () -> T) {

        private val lock = Any()
        private var cachedField: T? = null

        operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            synchronized(lock) {
                cachedField?.let { return it }
                val value = parent.getNodeAs(name, type) ?: defaultCreator()
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