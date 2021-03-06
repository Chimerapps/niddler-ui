package com.icapps.niddler.lib.codegen

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import java.util.Locale

/**
 * @author Nicola Verbeeck
 */
class CurlCodeGenerator : CodeGenerator {

    companion object {
        private const val COMMAND_NAME = "curl"
    }

    override fun generateRequestCode(request: NiddlerMessage): String {
        if (!request.isRequest)
            return "<not a request>"

        return when (request.method!!.lowercase(Locale.getDefault())) {
            "get" -> generateGet(request)
            "options" -> generateOptions(request)
            "delete" -> generateDelete(request)
            "head" -> generateHead(request)
            "post" -> generatePost(request)
            "put" -> generatePut(request)
            else -> "<request type not yet supported>"
        }
    }

    private fun generateOptions(request: NiddlerMessage): String {
        return generateNoBodyRequest(request, "OPTIONS")
    }

    private fun generateHead(request: NiddlerMessage): String {
        return generateNoBodyRequest(request, "HEAD")
    }

    private fun generateGet(request: NiddlerMessage): String {
        return generateNoBodyRequest(request, "GET")
    }

    private fun generateDelete(request: NiddlerMessage): String {
        return generateNoBodyRequest(request, "DELETE")
    }

    private fun generatePost(request: NiddlerMessage): String {
        return generateBodyRequest(request, "POST")
    }

    private fun generatePut(request: NiddlerMessage): String {
        return generateBodyRequest(request, "PUT")
    }

    private fun addHeaders(builder: StringBuilder, request: NiddlerMessage) {
        val headers = request.headers ?: return

        headers.forEach { (key, values) ->
            builder.append("-H \"").append(key).append(": ").append(values.joinToString(",")).append("\" ")
        }
    }

    private fun generateNoBodyRequest(request: NiddlerMessage, method: String): String {
        val builder = StringBuilder(COMMAND_NAME)
        builder.append(" -i ")
        addHeaders(builder, request)
        builder.append("-X \"").append(method.uppercase(Locale.getDefault())).append("\" \"")
        builder.append(request.url!!).append('"')
        addDecompress(builder, request)
        return builder.toString()
    }

    private fun generateBodyRequest(request: NiddlerMessage, method: String): String {
        val builder = StringBuilder(COMMAND_NAME)
        builder.append(" -i ")
        addHeaders(builder, request)
        builder.append("-X \"").append(method.uppercase(Locale.getDefault())).append("\" ")

        val bytes = request.getBodyAsBytes
        if (bytes != null) {
            builder.append("--data-binary \"")
            builder.append(escape(String(bytes, 0, bytes.size)))
            builder.append("\" ")
        }

        builder.append('"').append(request.url!!).append('"')
        addDecompress(builder, request)
        return builder.toString()
    }

    private fun escape(data: String): String {
        return data.replace("\"", "\\\"")
    }

    private fun addDecompress(builder: StringBuilder, message: NiddlerMessage) {
        val headers = message.headers ?: return
        val encodingHeaders = headers.entries.find { "accept-encoding".equals(it.key, ignoreCase = true) }?.value ?: return
        val isCompressing = encodingHeaders.any { headerValue ->
            headerValue.contains("gzip") || headerValue.contains("br") || headerValue.contains("deflate")
        }
        if (!isCompressing) return;

        builder.append(" --compressed")
    }

}