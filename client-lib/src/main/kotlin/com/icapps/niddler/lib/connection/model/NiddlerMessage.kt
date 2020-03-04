package com.icapps.niddler.lib.connection.model

import java.nio.charset.Charset
import java.util.Base64

/**
 * @author Nicola Verbeeck
 */
interface NiddlerMessage {

    val requestId: String
    val messageId: String

    val timestamp: Long

    val url: String?
    val method: String?
    val body: String?
    val headers: Map<String, List<String>>?
    val statusCode: Int?
    val statusLine: String?

    val writeTime: Int?
    val readTime: Int?
    val waitTime: Int?
    val httpVersion: String?

    val trace: List<String>?
    val context: List<String>?

    val networkRequest: NiddlerMessage?
    val networkReply: NiddlerMessage?

    val isRequest: Boolean
        get() = statusCode == null

    val getBodyAsBytes: ByteArray?
        get() = if (body != null) Base64.getUrlDecoder().decode(body) else null

    val bodyAsNormalBase64: String?
        get() = if (body != null) Base64.getEncoder().encodeToString(getBodyAsBytes) else null

    fun getBodyAsString(encoding: String?): String? {
        return if (body != null)
            String(Base64.getUrlDecoder().decode(body), if (encoding == null) Charsets.UTF_8
            else
                Charset.forName(encoding))
        else
            null
    }

}

inline val NiddlerMessage.isResponse : Boolean
    get() = !isRequest

val NiddlerMessage.isCachedResponse: Boolean
    get() = !isRequest && (networkReply?.statusCode == 304)

class NetworkNiddlerMessage(
        override val requestId: String,
        override val messageId: String,
        override val timestamp: Long,
        override val url: String? = null,
        override val method: String? = null,
        override val body: String? = null,
        override val headers: Map<String, List<String>>? = null,
        override val statusCode: Int? = null,
        override val statusLine: String? = null,
        override val writeTime: Int? = null,
        override val readTime: Int? = null,
        override val waitTime: Int? = null,
        override val httpVersion: String? = null,
        override val networkRequest: NiddlerMessage? = null,
        override val networkReply: NiddlerMessage? = null,
        override val trace: List<String>? = null,
        override val context: List<String>? = null
) : NiddlerMessage {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as NiddlerMessage

        if (messageId != other.messageId) return false

        return true
    }

    override fun hashCode(): Int {
        return messageId.hashCode()
    }

}

