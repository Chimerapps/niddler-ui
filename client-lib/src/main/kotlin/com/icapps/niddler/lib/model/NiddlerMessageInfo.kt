package com.icapps.niddler.lib.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.model.isCachedResponse
import com.icapps.niddler.lib.connection.model.isDebugOverride

data class NiddlerMessageInfo(
        val messageId: String,
        val requestId: String,
        val timestamp: Long,
        val url: String?,
        val type: NiddlerMessageType,
        val method: String?,
        val format: String?,
        val statusCode: Int?,
        val statusLine: String?,
        val hasBody: Boolean,
        val networkRequest: NiddlerMessageInfo?,
        val networkReply: NiddlerMessageInfo?
) {

    companion object {
        fun fromMessage(message: NiddlerMessage) : NiddlerMessageInfo {
            return NiddlerMessageInfo(
                    message.messageId,
                    message.requestId,
                    message.timestamp,
                    message.url,
                    type = determineType(message),
                    method = message.method,
                    format = determineFormat(message),
                    statusCode = message.statusCode,
                    statusLine = message.statusLine,
                    hasBody = !message.body.isNullOrEmpty(),
                    networkReply = message.networkReply?.let { fromMessage(it) },
                    networkRequest = message.networkRequest?.let { fromMessage(it) }
            )
        }

        private fun determineType(message: NiddlerMessage) : NiddlerMessageType {
            if (message.isRequest) {
                if (message.isDebugOverride) return NiddlerMessageType.UP_DEBUG
                return NiddlerMessageType.UP
            } else {
                if (message.isDebugOverride) return NiddlerMessageType.DOWN_DEBUG
                if (message.isCachedResponse) return NiddlerMessageType.DOWN_CACHED
                return NiddlerMessageType.DOWN
            }
        }

        private fun determineFormat(message: NiddlerMessage) : String?{
            return message.headers?.get("content-type")?.firstOrNull()
        }
    }

    val isRequest: Boolean
        get() = statusCode == null
}

enum class NiddlerMessageType {
    UP,
    DOWN,
    UP_DEBUG,
    DOWN_DEBUG,
    DOWN_CACHED
}