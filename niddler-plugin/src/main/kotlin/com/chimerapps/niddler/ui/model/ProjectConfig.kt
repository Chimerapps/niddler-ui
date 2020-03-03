package com.chimerapps.niddler.ui.model

import com.chimerapps.niddler.ui.util.ui.runWriteAction
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.intellij.project.isDirectoryBased

object ProjectConfig {

    private const val CONFIG_FILE_NAME = "niddler.json"
    const val CONFIG_REWRITE = "rewrite"

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var currentData: ConfigData? = null

    inline fun <reified T> load(project: Project, key: String) = load(project, key, T::class.java)
    inline fun <reified T> save(project: Project, key: String, data: T?) = save(project, key, data, T::class.java)

    fun <T> load(project: Project, key: String, clazz: Class<T>): T? {
        synchronized(gson) {
            currentData?.let { return it.data[key]?.let { value -> gson.fromJson(value, clazz) } }

            if (project.isDirectoryBased) {
                val dir = project.workspaceFile?.parent ?: return null

                dir.findChild(CONFIG_FILE_NAME)?.inputStream?.reader(Charsets.UTF_8)?.use {
                    currentData = gson.fromJson(it, ConfigData::class.java)
                }
            } else {
                return null
            }

            return currentData?.let { it.data[key]?.let { value -> gson.fromJson(value, clazz) } }
        }
    }

    fun <T> save(project: Project, key: String, data: T?, clazz: Class<T>) {
        synchronized(gson) {
            val dataHolder = getOrCreate()
            if (data == null) {
                if (dataHolder.data.remove(key) == null) return
            } else {
                dataHolder.data[key] = gson.toJsonTree(data)
            }

            if (project.isDirectoryBased) {
                val dir = project.workspaceFile?.parent ?: return

                runWriteAction {
                    val fileToWrite = dir.findOrCreateChildData(this, CONFIG_FILE_NAME)
                    fileToWrite.getOutputStream(this).writer(Charsets.UTF_8).use { writer ->
                        gson.toJson(dataHolder, writer)
                    }
                }
            }
        }
    }

    private fun getOrCreate(): ConfigData {
        if (currentData == null)
            currentData = ConfigData(mutableMapOf())
        return currentData!!
    }

}

private data class ConfigData(val data: MutableMap<String, JsonElement>)