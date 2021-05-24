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
)

fun interface FileResolver {

    fun resolveFile(file: String): String

}