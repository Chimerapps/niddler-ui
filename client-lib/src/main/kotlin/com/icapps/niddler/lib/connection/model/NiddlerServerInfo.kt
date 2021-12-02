package com.icapps.niddler.lib.connection.model

import com.google.gson.JsonObject

/**
 * @author Nicola Verbeeck
 */
data class NiddlerServerInfo(
    val serverName: String,
    val serverDescription: String,
    val icon: String?,
    val protocol: Int,
    val extensions: JsonObject?,
)