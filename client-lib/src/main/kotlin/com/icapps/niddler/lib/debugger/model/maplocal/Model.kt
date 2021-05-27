package com.icapps.niddler.lib.debugger.model.maplocal

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocation

/**
 * @author Nicola Verbeeck
 */
data class MapLocalConfiguration(
    val enabled: Boolean,
    val mappings: List<MapLocalEntry>,
)

data class MapLocalEntry(
    val location: RewriteLocation,
    val destination: String,
    val enabled: Boolean,
    val caseSensitive: Boolean,
    @Transient val id: String,
) {
    fun matchesUrl(url: String): Boolean {
        return Regex(location.asRegex()).matches(url)
    }
}

fun interface FileResolver {

    fun resolveFile(file: String): String

}

open class VariableFileResolver(
    protected val mapping: MutableMap<String, String>,
) : FileResolver {

    companion object {
        fun replacePrefix(source: String, variableName: String, prefix: String): String {
            return source.replace(prefix, "%$variableName%")
        }

        fun makeRelative(configuration: MapLocalConfiguration, variableName: String, relativeRoot: String): MapLocalConfiguration {
            return configuration.copy(mappings = configuration.mappings.map { e -> e.copy(destination = replacePrefix(e.destination, variableName, relativeRoot)) })
        }
    }

    override fun resolveFile(file: String): String {
        var resolved = file
        mapping.forEach { (key, value) ->
            resolved = resolved.replace("%$key%", value)
        }
        return resolved
    }

}