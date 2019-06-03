package com.icapps.niddler.lib.connection.model

/**
 * @author Nicola Verbeeck
 */
data class NiddlerServerInfo(val serverName: String,
                             val serverDescription: String,
                             val icon: String?,
                             val protocol: Int)